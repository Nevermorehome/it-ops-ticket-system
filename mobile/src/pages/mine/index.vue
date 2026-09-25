<template>
  <view class="page">
    <view class="profile">
      <view class="avatar">{{ (userStore.realName || 'U').charAt(0) }}</view>
      <view class="info">
        <text class="name">{{ userStore.realName || '未登录' }}</text>
        <text class="sub">
          {{ userStore.user.username }}
          <text v-if="roleText" class="roles">（{{ roleText }}）</text>
        </text>
      </view>
    </view>

    <view class="menu card">
      <view class="menu-item" @tap="pwdVisible = true">
        <text>修改密码</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @tap="showAbout">
        <text>关于系统</text>
        <text class="arrow">›</text>
      </view>
    </view>

    <view class="logout-wrap">
      <button class="logout" @tap="onLogout">退出登录</button>
    </view>

    <!-- 修改密码 -->
    <view v-if="pwdVisible" class="mask-layer" @tap="pwdVisible = false">
      <view class="dialog" @tap.stop>
        <view class="dialog-title">修改密码</view>
        <input v-model="pwd.oldPassword" class="inp" password placeholder="原密码" />
        <input v-model="pwd.newPassword" class="inp" password placeholder="新密码（至少 6 位）" />
        <input v-model="pwd.confirm" class="inp" password placeholder="确认新密码" />
        <view class="dialog-footer">
          <button class="btn-cancel" @tap="pwdVisible = false">取消</button>
          <button class="btn-ok" @tap="submitPwd">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'
import { authApi } from '@/api'

const userStore = useUserStore()
const pwdVisible = ref(false)
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })

const roleText = computed(() => {
  const map: Record<string, string> = {
    admin: '管理员',
    supervisor: '主管',
    engineer: '工程师',
    reporter: '报修人'
  }
  return userStore.roles.map((r) => map[r] || r).join('、')
})

async function ensureInfo() {
  if (!userStore.user.userId) {
    try {
      await userStore.fetchInfo()
    } catch {
      // ignore
    }
  }
}

function showAbout() {
  uni.showModal({
    title: '关于',
    content: '信息部内部 IT 运维工单记录系统 v1.0.0\n工单全流程 · 现场留痕 · 消息通知',
    showCancel: false
  })
}

async function submitPwd() {
  if (!pwd.oldPassword || !pwd.newPassword) {
    uni.showToast({ title: '请填写完整', icon: 'none' })
    return
  }
  if (pwd.newPassword.length < 6) {
    uni.showToast({ title: '新密码至少 6 位', icon: 'none' })
    return
  }
  if (pwd.newPassword !== pwd.confirm) {
    uni.showToast({ title: '两次输入不一致', icon: 'none' })
    return
  }
  await authApi.changePassword(pwd.oldPassword, pwd.newPassword)
  uni.showToast({ title: '密码修改成功，请重新登录', icon: 'none' })
  pwdVisible.value = false
  await userStore.reset()
  uni.reLaunch({ url: '/pages/login/index' })
}

async function onLogout() {
  const res = await uni.showModal({ title: '提示', content: '确定退出登录吗？' })
  if (!res.confirm) return
  await userStore.logout()
  uni.reLaunch({ url: '/pages/login/index' })
}

onShow(ensureInfo)
</script>

<style lang="scss" scoped>
.profile {
  background: linear-gradient(135deg, #2b6cb0, #1f3a68);
  padding: 60rpx 40rpx;
  display: flex;
  align-items: center;
}
.avatar {
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  font-size: 44rpx;
  text-align: center;
  line-height: 110rpx;
  margin-right: 28rpx;
}
.name {
  color: #fff;
  font-size: 38rpx;
  font-weight: 600;
  display: block;
}
.sub {
  color: rgba(255, 255, 255, 0.8);
  font-size: 26rpx;
  margin-top: 8rpx;
  display: block;
}
.roles {
  font-size: 24rpx;
}
.menu {
  margin-top: 20rpx;
  padding: 0 24rpx;
}
.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 0;
  border-bottom: 1rpx solid #f2f3f5;
}
.menu-item:last-child {
  border-bottom: none;
}
.arrow {
  color: #c0c4cc;
  font-size: 40rpx;
}
.logout-wrap {
  padding: 40rpx;
}
.logout {
  background: #fff;
  color: #f56c6c;
  border-radius: 12rpx;
  font-size: 30rpx;
}
.logout::after {
  border: none;
}
.mask-layer {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}
.dialog {
  width: 600rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 36rpx;
}
.dialog-title {
  font-weight: 600;
  text-align: center;
  margin-bottom: 28rpx;
  font-size: 32rpx;
}
.inp {
  background: #f7f8fa;
  border-radius: 10rpx;
  padding: 22rpx 20rpx;
  margin-bottom: 20rpx;
}
.dialog-footer {
  display: flex;
  gap: 20rpx;
  margin-top: 12rpx;
}
.btn-cancel,
.btn-ok {
  flex: 1;
  border-radius: 10rpx;
  font-size: 28rpx;
}
.btn-cancel {
  background: #f4f5f7;
  color: #606266;
}
.btn-cancel::after {
  border: none;
}
.btn-ok {
  background: #2b6cb0;
  color: #fff;
}
.btn-ok::after {
  border: none;
}
</style>
