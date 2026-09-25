<template>
  <div class="app-container">
    <el-form class="search-form" :inline="true" @submit.prevent>
      <el-form-item label="账号">
        <el-input v-model="query.username" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="query.realName" clearable style="width: 120px" />
      </el-form-item>
      <el-form-item label="部门">
        <el-tree-select
          v-model="query.deptId"
          :data="deptTree"
          :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
          check-strictly
          clearable
          style="width: 170px"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable style="width: 100px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
        <el-button :icon="Refresh" @click="onReset">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="table-card">
      <div class="toolbar">
        <el-button v-hasPermi="['system:user:add']" type="primary" :icon="Plus" @click="openDialog()">
          新增用户
        </el-button>
      </div>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="username" label="账号" width="130" />
        <el-table-column prop="realName" label="姓名" width="110" />
        <el-table-column label="部门" width="150">
          <template #default="{ row }">{{ deptNameMap[row.deptId] || '—' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="170" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="['system:user:edit']" link type="primary" size="small" @click="openDialog(row)">
              修改
            </el-button>
            <el-button v-hasPermi="['system:user:reset']" link type="warning" size="small" @click="openReset(row)">
              重置密码
            </el-button>
            <el-button v-hasPermi="['system:user:remove']" link type="danger" size="small" @click="remove(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="load"
          @current-change="load"
        />
      </div>
    </div>

    <el-dialog v-model="visible" :title="form.userId ? '编辑用户' : '新增用户'" width="640px">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="账号" required>
              <el-input v-model="form.username" :disabled="!!form.userId" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" required>
              <el-input v-model="form.realName" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="!form.userId">
            <el-form-item label="初始密码" required>
              <el-input v-model="form.password" placeholder="至少 6 位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门">
              <el-tree-select
                v-model="form.deptId"
                :data="deptTree"
                :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
                check-strictly
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.sex">
                <el-radio value="0">男</el-radio>
                <el-radio value="1">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio value="0">正常</el-radio>
                <el-radio value="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="角色">
              <el-select v-model="roleIds" multiple placeholder="分配角色" style="width: 100%">
                <el-option v-for="r in roles" :key="r.roleId" :label="r.roleName" :value="Number(r.roleId)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resetVisible" title="重置密码" width="400px">
      <el-form label-width="90px">
        <el-form-item label="用户">{{ resetRow?.realName }}（{{ resetRow?.username }}）</el-form-item>
        <el-form-item label="新密码" required>
          <el-input v-model="newPassword" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReset">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deptApi, roleApi, userApi } from '@/api/system'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const deptTree = ref<any[]>([])
const deptNameMap = ref<Record<string, string>>({})
const roles = ref<any[]>([])

const query = reactive<any>({
  pageNum: 1,
  pageSize: 10,
  username: '',
  realName: '',
  deptId: undefined,
  status: ''
})

const visible = ref(false)
const form = reactive<any>({})
const roleIds = ref<number[]>([])

const resetVisible = ref(false)
const resetRow = ref<any>(null)
const newPassword = ref('')

function flatten(nodes: any[], map: Record<string, string>) {
  nodes.forEach((n) => {
    map[n.deptId] = n.deptName
    if (n.children?.length) flatten(n.children, map)
  })
}

async function load() {
  loading.value = true
  try {
    const res = await userApi.page(query)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}
function onSearch() {
  query.pageNum = 1
  load()
}
function onReset() {
  Object.assign(query, { username: '', realName: '', deptId: undefined, status: '' })
  onSearch()
}

async function openDialog(row?: any) {
  if (row) {
    const res = await userApi.detail(row.userId)
    Object.keys(form).forEach((k) => delete (form as any)[k])
    Object.assign(form, res.data.user)
    roleIds.value = (res.data.roleIds || []).map(Number)
  } else {
    Object.keys(form).forEach((k) => delete (form as any)[k])
    Object.assign(form, {
      username: '',
      realName: '',
      password: '',
      deptId: undefined,
      phone: '',
      email: '',
      sex: '0',
      status: '0',
      remark: ''
    })
    roleIds.value = []
  }
  visible.value = true
}

async function submit() {
  if (!form.username || !form.realName) {
    ElMessage.warning('账号和姓名必填')
    return
  }
  if (!form.userId && (!form.password || form.password.length < 6)) {
    ElMessage.warning('初始密码至少 6 位')
    return
  }
  if (form.userId) {
    await userApi.update(form, roleIds.value)
  } else {
    await userApi.create(form, roleIds.value)
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

function openReset(row: any) {
  resetRow.value = row
  newPassword.value = ''
  resetVisible.value = true
}
async function submitReset() {
  if (!newPassword.value || newPassword.value.length < 6) {
    ElMessage.warning('新密码至少 6 位')
    return
  }
  await userApi.resetPwd(resetRow.value.userId, newPassword.value)
  ElMessage.success('密码已重置')
  resetVisible.value = false
}

async function remove(row: any) {
  if (Number(row.userId) === 1) {
    ElMessage.warning('超级管理员不允许删除')
    return
  }
  await ElMessageBox.confirm(`确定删除用户「${row.realName}」吗？`, '提示', { type: 'warning' })
  await userApi.remove(row.userId)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  const [dept, role] = await Promise.all([deptApi.tree(), roleApi.all()])
  deptTree.value = dept.data || []
  flatten(deptTree.value, deptNameMap.value)
  roles.value = role.data || []
  load()
})
</script>
