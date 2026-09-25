<template>
  <div class="app-container">
    <el-form class="search-form" :inline="true" @submit.prevent>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="资产编号/名称" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="类别">
        <el-input v-model="query.category" placeholder="设备类别" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" />
        </el-select>
      </el-form-item>
      <el-form-item label="使用部门">
        <el-tree-select
          v-model="query.deptId"
          :data="deptTree"
          :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
          check-strictly
          clearable
          placeholder="全部"
          style="width: 170px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
        <el-button :icon="Refresh" @click="onReset">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="table-card">
      <div class="toolbar">
        <el-button v-hasPermi="['base:asset:add']" type="primary" :icon="Plus" @click="openDialog()">
          新增资产
        </el-button>
      </div>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="assetNo" label="资产编号" width="150" class-name="mono" />
        <el-table-column prop="assetName" label="资产名称" min-width="150" />
        <el-table-column prop="category" label="类别" width="110" />
        <el-table-column prop="brand" label="品牌" width="110" />
        <el-table-column prop="model" label="型号" width="130" show-overflow-tooltip />
        <el-table-column prop="userName" label="使用人" width="90">
          <template #default="{ row }">{{ row.userName || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="assetStatusType(row.status)" size="small">
              {{ assetStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="buyDate" label="购入日期" width="110" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button v-hasPermi="['base:asset:edit']" link type="primary" size="small" @click="openDialog(row)">
              修改
            </el-button>
            <el-button v-hasPermi="['base:asset:remove']" link type="danger" size="small" @click="remove(row)">
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

    <el-dialog v-model="visible" :title="form.assetId ? '编辑资产' : '新增资产'" width="640px">
      <el-form :model="form" label-width="84px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="资产编号">
              <el-input v-model="form.assetNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产名称" required>
              <el-input v-model="form.assetName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类别">
              <el-input v-model="form.category" placeholder="如：台式机/打印机" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-input v-model="form.brand" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号">
              <el-input v-model="form.model" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="使用部门">
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
            <el-form-item label="使用人">
              <el-select
                v-model="form.userId"
                filterable
                remote
                :remote-method="searchUsers"
                :loading="userLoading"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="u in userOptions"
                  :key="u.userId"
                  :label="`${u.realName}(${u.username})`"
                  :value="Number(u.userId)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购入日期">
              <el-date-picker v-model="form.buyDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="存放位置">
              <el-input v-model="form.locationText" />
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
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { assetApi } from '@/api/base'
import { deptApi } from '@/api/system'
import { userOptions as fetchUserOptions } from '@/api/ticket'
import { fallbackDict, useDictData, type DictItem } from '@/composables/useDict'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const deptTree = ref<any[]>([])
const userOptions = ref<any[]>([])
const userLoading = ref(false)
let statusDict: DictItem[] = [
  { dictLabel: '在用', dictValue: '0', listClass: 'success' },
  { dictLabel: '闲置', dictValue: '1', listClass: 'info' },
  { dictLabel: '维修', dictValue: '2', listClass: 'warning' },
  { dictLabel: '报废', dictValue: '3', listClass: 'danger' }
]

const query = reactive<any>({ pageNum: 1, pageSize: 10, keyword: '', category: '', status: '', deptId: undefined })
const visible = ref(false)
const form = reactive<any>({})

function assetStatusLabel(s: string) {
  return statusDict.find((d) => d.dictValue === s)?.dictLabel || s || '—'
}
function assetStatusType(s: string): any {
  return (statusDict.find((d) => d.dictValue === s)?.listClass || 'info') as any
}

async function load() {
  loading.value = true
  try {
    const res = await assetApi.page(query)
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
  Object.assign(query, { keyword: '', category: '', status: '', deptId: undefined })
  onSearch()
}

async function searchUsers(keyword: string) {
  userLoading.value = true
  try {
    const res = await fetchUserOptions(keyword)
    userOptions.value = res.data || []
  } finally {
    userLoading.value = false
  }
}

function openDialog(row?: any) {
  if (row) {
    Object.keys(form).forEach((k) => delete (form as any)[k])
    Object.assign(form, JSON.parse(JSON.stringify(row)))
  } else {
    Object.keys(form).forEach((k) => delete (form as any)[k])
    Object.assign(form, { status: '0', deptId: undefined, userId: undefined })
  }
  visible.value = true
}

async function submit() {
  if (!form.assetName?.trim()) {
    ElMessage.warning('请输入资产名称')
    return
  }
  if (form.assetId) {
    await assetApi.update({ ...form })
  } else {
    await assetApi.create({ ...form })
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确定删除资产「${row.assetName}」吗？`, '提示', { type: 'warning' })
  await assetApi.remove(row.assetId)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  statusDict = await useDictData('asset_status')
  const dept = await deptApi.tree()
  deptTree.value = dept.data || []
  searchUsers('')
  load()
})
</script>
