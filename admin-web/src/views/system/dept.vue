<template>
  <div class="app-container">
    <div class="table-card">
      <div class="toolbar">
        <el-button v-hasPermi="['system:dept:add']" type="primary" :icon="Plus" @click="openDialog(undefined, 0)">
          新增部门
        </el-button>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
      <el-table v-loading="loading" :data="tree" row-key="deptId" border default-expand-all>
        <el-table-column prop="deptName" label="部门名称" width="240" />
        <el-table-column prop="leader" label="负责人" width="130" />
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column prop="orderNum" label="排序" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-hasPermi="['system:dept:add']" link type="primary" size="small" @click="openDialog(undefined, row.deptId)">
              新增下级
            </el-button>
            <el-button v-hasPermi="['system:dept:edit']" link type="primary" size="small" @click="openDialog(row)">
              修改
            </el-button>
            <el-button v-hasPermi="['system:dept:remove']" link type="danger" size="small" @click="remove(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="form.deptId ? '编辑部门' : '新增部门'" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
            check-strictly
            clearable
            placeholder="顶级部门"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" required>
          <el-input v-model="form.deptName" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.leader" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.orderNum" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deptApi } from '@/api/system'

const loading = ref(false)
const tree = ref<any[]>([])
const visible = ref(false)
const form = reactive<any>({ deptId: undefined, parentId: 0, deptName: '', leader: '', phone: '', orderNum: 0, status: '0' })
const parentOptions = computed(() => [{ deptId: 0, deptName: '顶级部门', children: tree.value }])

async function load() {
  loading.value = true
  try {
    const res = await deptApi.tree()
    tree.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any, parentId?: any) {
  if (row) {
    Object.assign(form, { ...row, parentId: Number(row.parentId) || 0 })
  } else {
    Object.assign(form, {
      deptId: undefined,
      parentId: parentId ? Number(parentId) : 0,
      deptName: '',
      leader: '',
      phone: '',
      orderNum: 0,
      status: '0'
    })
  }
  visible.value = true
}

async function submit() {
  if (!form.deptName) {
    ElMessage.warning('请输入部门名称')
    return
  }
  const payload = { ...form }
  if (!payload.parentId) payload.parentId = 0
  if (form.deptId) {
    await deptApi.update(payload)
  } else {
    await deptApi.create(payload)
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确定删除部门「${row.deptName}」吗？下级部门需先删除。`, '提示', { type: 'warning' })
  await deptApi.remove(row.deptId)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
