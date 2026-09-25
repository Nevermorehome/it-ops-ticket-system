<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="aside">
      <div class="logo">
        <el-icon :size="22"><Monitor /></el-icon>
        <span v-show="!collapsed" class="logo-text">IT 运维工单</span>
      </div>
      <el-scrollbar class="menu-scroll">
        <el-menu
          :default-active="activeMenu"
          :collapse="collapsed"
          :collapse-transition="false"
          router
          background-color="#001529"
          text-color="#b8c2d0"
          active-text-color="#ffffff"
        >
          <template v-for="m in menuTree" :key="m.menuId">
            <el-sub-menu v-if="m.menuType === 'M'" :index="m.path">
              <template #title>
                <el-icon><component :is="m.icon || 'Menu'" /></el-icon>
                <span>{{ m.menuName }}</span>
              </template>
              <el-menu-item
                v-for="c in visibleChildren(m)"
                :key="c.menuId"
                :index="`${m.path}/${c.path}`.replace('//', '/')"
              >
                <el-icon><component :is="c.icon || 'Document'" /></el-icon>
                <span>{{ c.menuName }}</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="m.path">
              <el-icon><component :is="m.icon || 'Document'" /></el-icon>
              <template #title>{{ m.menuName }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" :size="20" @click="collapsed = !collapsed">
            <Fold v-if="!collapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="b in breadcrumbs" :key="b.path" :to="b.path">
              {{ b.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-popover placement="bottom-end" :width="360" trigger="click" @show="loadMessages">
            <template #reference>
              <el-badge :value="unread" :hidden="unread === 0" :max="99" class="bell">
                <el-icon :size="20"><Bell /></el-icon>
              </el-badge>
            </template>
            <div class="msg-panel">
              <div class="msg-head">
                <span>站内消息 ({{ unread }} 未读)</span>
                <el-button link type="primary" size="small" @click="readAll">全部已读</el-button>
              </div>
              <el-scrollbar height="280px">
                <div
                  v-for="msg in messages"
                  :key="msg.messageId"
                  class="msg-item"
                  :class="{ unread: msg.isRead === 0 }"
                  @click="openMessage(msg)"
                >
                  <div class="msg-title">{{ msg.title }}</div>
                  <div class="msg-content">{{ msg.content }}</div>
                  <div class="msg-time">{{ msg.createTime }}</div>
                </div>
                <el-empty v-if="messages.length === 0" description="暂无消息" :image-size="60" />
              </el-scrollbar>
            </div>
          </el-popover>

          <el-dropdown @command="onCommand">
            <span class="user-info">
              <el-avatar :size="30" class="avatar">
                {{ (userStore.user.realName || userStore.user.username || 'U').charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.user.realName || userStore.user.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <el-dialog v-model="pwdVisible" title="修改密码" width="420px">
      <el-form :model="pwdForm" label-width="90px">
        <el-form-item label="原密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="pwdForm.confirm" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPwd">确定</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { resetRouterState } from '@/router'
import { changePassword } from '@/api/auth'
import { messageApi } from '@/api/message'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const collapsed = ref(false)
const menuTree = computed(() =>
  [...userStore.menus]
    .filter((m: any) => m.menuType === 'M' || m.menuType === 'C')
    .sort((a: any, b: any) => (a.orderNum || 0) - (b.orderNum || 0))
)
function visibleChildren(m: any) {
  return (m.children || [])
    .filter((c: any) => c.menuType === 'C')
    .sort((a: any, b: any) => (a.orderNum || 0) - (b.orderNum || 0))
}

const activeMenu = computed(() => (route.meta.activeMenu as string) || route.path)

const breadcrumbs = computed(() => {
  return route.matched
    .filter((r) => r.meta?.title)
    .map((r) => ({ path: r.path, title: r.meta.title as string }))
})

// ---------------- 消息铃铛 ----------------
const unread = ref(0)
const messages = ref<any[]>([])
let timer: any

async function refreshUnread() {
  try {
    const res = await messageApi.unreadCount()
    unread.value = Number(res.data || 0)
  } catch {
    // ignore
  }
}
async function loadMessages() {
  const res = await messageApi.page({ pageNum: 1, pageSize: 10 })
  messages.value = res.data.records || []
  refreshUnread()
}
async function openMessage(msg: any) {
  if (msg.isRead === 0) {
    await messageApi.read(msg.messageId)
    msg.isRead = 1
    refreshUnread()
  }
  router.push('/message/list')
}
async function readAll() {
  await messageApi.readAll()
  ElMessage.success('已全部标记为已读')
  loadMessages()
}

onMounted(() => {
  refreshUnread()
  timer = setInterval(refreshUnread, 60000)
})
onBeforeUnmount(() => clearInterval(timer))

// ---------------- 用户操作 ----------------
const pwdVisible = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirm: '' })

async function onCommand(cmd: string) {
  if (cmd === 'password') {
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirm = ''
    pwdVisible.value = true
  } else if (cmd === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
    await userStore.logout()
    resetRouterState()
    router.replace('/login')
  }
}

async function submitPwd() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirm) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  await changePassword(pwdForm.oldPassword, pwdForm.newPassword)
  ElMessage.success('密码修改成功，请重新登录')
  pwdVisible.value = false
  await userStore.logout()
  resetRouterState()
  router.replace('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  background: #001529;
  transition: width 0.2s;
  overflow: hidden;
}
.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-weight: 600;
  letter-spacing: 1px;
}
.logo-text {
  white-space: nowrap;
}
.menu-scroll {
  height: calc(100% - 56px);
}
.aside :deep(.el-menu) {
  border-right: none;
}
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 18px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.collapse-btn {
  cursor: pointer;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 22px;
}
.bell {
  cursor: pointer;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.avatar {
  background: #409eff;
}
.username {
  font-size: 14px;
}
.main {
  background: #f0f2f5;
  padding: 0;
}
.main :deep(.el-main) {
  padding: 0;
}
.msg-panel {
  margin: -12px;
}
.msg-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  border-bottom: 1px solid #eee;
  font-weight: 600;
}
.msg-item {
  padding: 10px 14px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
}
.msg-item:hover {
  background: #f5f7fa;
}
.msg-item.unread .msg-title {
  font-weight: 600;
  color: #303133;
}
.msg-title {
  font-size: 14px;
  color: #606266;
}
.msg-content {
  font-size: 12px;
  color: #909399;
  margin: 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.msg-time {
  font-size: 11px;
  color: #c0c4cc;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
