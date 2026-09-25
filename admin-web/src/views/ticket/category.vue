<template>
  <div class="app-container">
    <div class="table-card">
      <div class="toolbar">
        <el-button v-hasPermi="['ticket:category:list']" type="primary" :icon="Plus" @click="openDialog()">
          新增分类
        </el-button>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
      <el-table v-loading="loading" :data="tree" row-key="categoryId" border default-expand-all>
        <el-table-column prop="categoryName" label="分类名称" width="240" />
        <el-table-column prop="orderNum" label="排序" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'info'" size="small">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button v-hasPermi="['ticket:category:list']" link type="primary" size="small" @click="openDialog(row)">
              修改
            </el-button>
            <el-button
              v-hasPermi="['ticket:category:list']"
              link
              type="primary"
              size="small"
              @click="openDialog(undefined, row.categoryId)"
            >
              新增子级
            </el-button>
            <el-button v-hasPermi="['ticket:category:list']" link type="danger" size="small" @click="remove(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="form.categoryId ? '编辑分类' : '新增分类'" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="上级分类">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'categoryName', value: 'categoryId', children: 'children' }"
            check-strictly
            clearable
            placeholder="顶级分类"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="分类名称" required>
          <el-input v-model="form.categoryName" placeholder="分类名称" />
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
import { categoryApi } from '@/api/base'

const loading = ref(false)
const tree = ref<any[]>([])
const visible = ref(false)
const form = reactive<any>({ categoryId: undefined, parentId: 0, categoryName: '', orderNum: 0, status: '0' })

const parentOptions = computed(() => [{ categoryId: 0, categoryName: '顶级分类', children: tree.value }])

async function load() {
  loading.value = true
  try {
    const res = await categoryApi.tree()
    tree.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any, parentId?: any) {
  if (row) {
    Object.assign(form, {
      categoryId: row.categoryId,
      parentId: Number(row.parentId) || 0,
      categoryName: row.categoryName,
      orderNum: row.orderNum ?? 0,
      status: row.status || '0'
    })
  } else {
    Object.assign(form, {
      categoryId: undefined,
      parentId: parentId ? Number(parentId) : 0,
      categoryName: '',
      orderNum: 0,
      status: '0'
    })
  }
  visible.value = true
}

async function submit() {
  if (!form.categoryName?.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  const payload: any = { ...form }
  if (!payload.parentId) payload.parentId = 0
  if (form.categoryId) {
    await categoryApi.update(payload)
  } else {
    await categoryApi.create(payload)
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确定删除分类「${row.categoryName}」吗？子分类需先删除。`, '提示', { type: 'warning' })
  await categoryApi.remove(row.categoryId)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
