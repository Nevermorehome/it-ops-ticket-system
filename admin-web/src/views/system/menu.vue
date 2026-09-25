<template>
  <div class="app-container">
    <el-form class="search-form" :inline="true" @submit.prevent>
      <el-form-item label="菜单名称">
        <el-input v-model="query.menuName" clearable style="width: 160px" @keyup.enter="load" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
      </el-form-item>
    </el-form>

    <div class="table-card">
      <div class="toolbar">
        <el-button v-hasPermi="['system:menu:add']" type="primary" :icon="Plus" @click="openDialog(undefined, 0)">
          新增菜单
        </el-button>
      </div>
      <el-table
        v-loading="loading"
        :data="filteredTree"
        row-key="menuId"
        border
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="180" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.menuType)" size="small">{{ typeLabel(row.menuType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" width="130" />
        <el-table-column prop="component" label="组件" width="170" />
        <el-table-column prop="perms" label="权限标识" width="190" />
        <el-table-column prop="icon" label="图标" width="120" />
        <el-table-column prop="orderNum" label="排序" width="70" />
        <el-table-column label="可见" width="70">
          <template #default="{ row }">{{ row.visible === '1' ? '隐藏' : '显示' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <el-button v-hasPermi="['system:menu:add']" link type="primary" size="small" @click="openDialog(undefined, row.menuId)">
              新增
            </el-button>
            <el-button v-hasPermi="['system:menu:edit']" link type="primary" size="small" @click="openDialog(row)">
              修改
            </el-button>
            <el-button v-hasPermi="['system:menu:remove']" link type="danger" size="small" @click="remove(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="form.menuId ? '编辑菜单' : '新增菜单'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="类型">
              <el-radio-group v-model="form.menuType">
                <el-radio value="M">目录</el-radio>
                <el-radio value="C">菜单</el-radio>
                <el-radio value="F">按钮</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上级菜单">
              <el-tree-select
                v-model="form.parentId"
                :data="parentOptions"
                :props="{ label: 'menuName', value: 'menuId', children: 'children' }"
                check-strictly
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单名称" required>
              <el-input v-model="form.menuName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.orderNum" :min="0" :max="999" />
            </el-form-item>
          </el-col>
          <template v-if="form.menuType !== 'F'">
            <el-col :span="12">
              <el-form-item label="路由路径">
                <el-input v-model="form.path" placeholder="如 /ticket 或 list" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="组件路径">
                <el-input v-model="form.component" placeholder="如 ticket/index" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="图标">
                <el-input v-model="form.icon" placeholder="Element 图标名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="是否可见">
                <el-radio-group v-model="form.visible">
                  <el-radio value="0">显示</el-radio>
                  <el-radio value="1">隐藏</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </template>
          <el-col v-if="form.menuType !== 'M'" :span="12">
            <el-form-item label="权限标识">
              <el-input v-model="form.perms" placeholder="如 ticket:list" />
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
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { menuApi } from '@/api/system'

const loading = ref(false)
const tree = ref<any[]>([])
const query = reactive({ menuName: '' })
const visible = ref(false)
const form = reactive<any>({
  menuId: undefined,
  parentId: 0,
  menuName: '',
  menuType: 'C',
  path: '',
  component: '',
  perms: '',
  icon: '',
  orderNum: 1,
  visible: '0',
  status: '0'
})

const parentOptions = computed(() => [{ menuId: 0, menuName: '顶级', children: tree.value }])

const filteredTree = computed(() => {
  if (!query.menuName) return tree.value
  const kw = query.menuName
  const filterNode = (nodes: any[]): any[] =>
    nodes
      .map((n) => ({ ...n, children: n.children ? filterNode(n.children) : [] }))
      .filter((n) => n.menuName.includes(kw) || n.children.length)
  return filterNode(tree.value)
})

function typeLabel(t: string) {
  return { M: '目录', C: '菜单', F: '按钮' }[t] || t
}
function typeTag(t: string): any {
  return ({ M: 'primary', C: 'success', F: 'info' } as any)[t] || 'info'
}

async function load() {
  loading.value = true
  try {
    const res = await menuApi.tree()
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
      menuId: undefined,
      parentId: parentId ? Number(parentId) : 0,
      menuName: '',
      menuType: 'C',
      path: '',
      component: '',
      perms: '',
      icon: '',
      orderNum: 1,
      visible: '0',
      status: '0'
    })
  }
  visible.value = true
}

async function submit() {
  if (!form.menuName) {
    ElMessage.warning('请输入菜单名称')
    return
  }
  const payload = { ...form }
  if (!payload.parentId) payload.parentId = 0
  if (form.menuId) {
    await menuApi.update(payload)
  } else {
    await menuApi.create(payload)
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确定删除菜单「${row.menuName}」吗？子菜单需先删除。`, '提示', { type: 'warning' })
  await menuApi.remove(row.menuId)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
