<template>
  <div class="app-container">
    <el-form class="search-form" :inline="true" @submit.prevent>
      <el-form-item label="角色名称">
        <el-input v-model="query.roleName" clearable style="width: 160px" />
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
        <el-button v-hasPermi="['system:role:add']" type="primary" :icon="Plus" @click="openDialog()">
          新增角色
        </el-button>
      </div>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="roleId" label="ID" width="70" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleKey" label="权限标识" width="150" />
        <el-table-column label="数据范围" width="150">
          <template #default="{ row }">{{ scopeLabel(row.dataScope) }}</template>
        </el-table-column>
        <el-table-column prop="orderNum" label="排序" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button v-hasPermi="['system:role:edit']" link type="primary" size="small" @click="openDialog(row)">
              修改
            </el-button>
            <el-button v-hasPermi="['system:role:remove']" link type="danger" size="small" @click="remove(row)">
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
          layout="total, prev, pager, next"
          @current-change="load"
        />
      </div>
    </div>

    <el-dialog v-model="visible" :title="form.roleId ? '编辑角色' : '新增角色'" width="640px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="角色名称" required>
              <el-input v-model="form.roleName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限标识" required>
              <el-input v-model="form.roleKey" placeholder="如 engineer" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.orderNum" :min="0" :max="99" />
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
        </el-row>
        <el-form-item label="数据权限">
          <el-radio-group v-model="form.dataScope">
            <el-radio value="1">全部数据</el-radio>
            <el-radio value="2">自定义部门</el-radio>
            <el-radio value="3">本部门</el-radio>
            <el-radio value="4">本部门及以下</el-radio>
            <el-radio value="5">仅本人</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.dataScope === '2'" label="授权部门">
          <el-tree
            ref="deptTreeRef"
            :data="deptTreeData"
            :props="{ label: 'deptName', children: 'children' }"
            node-key="deptId"
            show-checkbox
            default-expand-all
            style="max-height: 200px; overflow: auto"
          />
        </el-form-item>
        <el-form-item label="菜单权限">
          <div class="menu-tree-head">
          </div>
          <el-tree
            ref="menuTreeRef"
            :data="menuTreeData"
            :props="menuProps"
            node-key="menuId"
            show-checkbox
            default-expand-all
            style="max-height: 300px; overflow: auto; border: 1px solid #ebeef5; padding: 8px; border-radius: 4px"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deptApi, menuApi, roleApi } from '@/api/system'

const SCOPE_MAP: Record<string, string> = {
  '1': '全部数据',
  '2': '自定义部门',
  '3': '本部门',
  '4': '本部门及以下',
  '5': '仅本人'
}
function scopeLabel(s: string) {
  return SCOPE_MAP[s] || '—'
}

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, roleName: '', status: '' })
const visible = ref(false)
const form = reactive<any>({ dataScope: '5', orderNum: 1, status: '0' })
const menuTreeData = ref<any[]>([])
const deptTreeData = ref<any[]>([])
const menuTreeRef = ref()
const deptTreeRef = ref()
const menuProps = { label: 'menuName', children: 'children' }

async function load() {
  loading.value = true
  try {
    const res = await roleApi.page(query)
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
  query.roleName = ''
  query.status = ''
  onSearch()
}

async function openDialog(row?: any) {
  Object.keys(form).forEach((k) => delete (form as any)[k])
  if (row) {
    const res = await roleApi.detail(row.roleId)
    Object.assign(form, res.data, {
      menuIds: (res.data.menuIds || []).map(Number),
      deptIds: (res.data.deptIds || []).map(Number)
    })
  } else {
    Object.assign(form, {
      roleName: '',
      roleKey: '',
      dataScope: '5',
      orderNum: 1,
      status: '0',
      remark: '',
      menuIds: [],
      deptIds: []
    })
  }
  visible.value = true
  await nextTick()
  menuTreeRef.value?.setCheckedKeys(form.menuIds || [])
  deptTreeRef.value?.setCheckedKeys(form.deptIds || [])
}

async function submit() {
  if (!form.roleName || !form.roleKey) {
    ElMessage.warning('角色名称与权限标识必填')
    return
  }
  // 菜单勾选: 包含半选父节点
  const checked = menuTreeRef.value?.getCheckedKeys() || []
  const half = menuTreeRef.value?.getHalfCheckedKeys() || []
  form.menuIds = [...checked, ...half].map(Number)
  if (form.dataScope === '2') {
    const depChecked = deptTreeRef.value?.getCheckedKeys() || []
    const depHalf = deptTreeRef.value?.getHalfCheckedKeys() || []
    form.deptIds = [...depChecked, ...depHalf].map(Number)
    if (!form.deptIds.length) {
      ElMessage.warning('请勾选授权部门')
      return
    }
  } else {
    form.deptIds = []
  }
  if (form.roleId) {
    await roleApi.update({ ...form })
  } else {
    await roleApi.create({ ...form })
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确定删除角色「${row.roleName}」吗？`, '提示', { type: 'warning' })
  await roleApi.remove(row.roleId)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  const [menu, dept] = await Promise.all([menuApi.tree(), deptApi.tree()])
  menuTreeData.value = menu.data || []
  deptTreeData.value = dept.data || []
  load()
})
</script>
