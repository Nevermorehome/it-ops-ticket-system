<template>
  <div class="app-container">
    <el-form class="search-form" :inline="true" :model="query" @submit.prevent>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="编号 / 标题" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
          <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级">
        <el-select v-model="query.priority" placeholder="全部" clearable style="width: 110px">
          <el-option
            v-for="d in priorityDict"
            :key="d.dictValue"
            :label="d.dictLabel"
            :value="Number(d.dictValue)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="来源">
        <el-select v-model="query.source" placeholder="全部" clearable style="width: 130px">
          <el-option v-for="d in sourceDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类">
        <el-tree-select
          v-model="query.categoryId"
          :data="categoryTree"
          :props="{ label: 'categoryName', value: 'categoryId', children: 'children' }"
          check-strictly
          clearable
          placeholder="全部"
          style="width: 170px"
        />
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始"
          end-placeholder="结束"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
        <el-button :icon="Refresh" @click="onReset">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="table-card">
      <div class="toolbar">
        <el-button v-hasPermi="['ticket:add']" type="primary" :icon="Plus" @click="goCreate">
          新建工单
        </el-button>
      </div>
      <el-table v-loading="loading" :data="list" border stripe @row-click="goDetail">
        <el-table-column prop="ticketNo" label="工单编号" width="168" class-name="mono" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small">
              {{ priorityLabel(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="92" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reporterName" label="报修人" width="90" />
        <el-table-column prop="deptName" label="部门" width="120" show-overflow-tooltip />
        <el-table-column prop="handlerName" label="处理人" width="90">
          <template #default="{ row }">{{ row.handlerName || '—' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="90" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click.stop="goDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="load"
          @current-change="load"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { pageTicket } from '@/api/ticket'
import { categoryApi } from '@/api/base'
import { fallbackDict, useDictData, type DictItem } from '@/composables/useDict'

const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const dateRange = ref<string[]>()
const categoryTree = ref<any[]>([])

const query = reactive<any>({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: '',
  priority: undefined,
  source: '',
  categoryId: undefined
})

let statusDict: DictItem[] = fallbackDict('ticket_status')
let priorityDict: DictItem[] = fallbackDict('ticket_priority')
let sourceDict: DictItem[] = fallbackDict('ticket_source')

function statusLabel(s: string) {
  return statusDict.find((d) => d.dictValue === s)?.dictLabel || s
}
function statusType(s: string): any {
  return (statusDict.find((d) => d.dictValue === s)?.listClass || 'info') as any
}
function priorityLabel(p: number) {
  return priorityDict.find((d) => Number(d.dictValue) === p)?.dictLabel || '—'
}
function priorityType(p: number): any {
  return (priorityDict.find((d) => Number(d.dictValue) === p)?.listClass || 'info') as any
}

async function load() {
  loading.value = true
  try {
    if (dateRange.value && dateRange.value.length === 2) {
      query.beginTime = `${dateRange.value[0]} 00:00:00`
      query.endTime = `${dateRange.value[1]} 23:59:59`
    } else {
      query.beginTime = undefined
      query.endTime = undefined
    }
    const res = await pageTicket(query)
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
  Object.assign(query, {
    keyword: '',
    status: '',
    priority: undefined,
    source: '',
    categoryId: undefined
  })
  dateRange.value = undefined
  onSearch()
}
function goCreate() {
  router.push('/ticket/create')
}
function goDetail(row: any) {
  router.push(`/ticket/detail/${row.ticketId}`)
}

onMounted(async () => {
  const [s, p, so] = await Promise.all([
    useDictData('ticket_status'),
    useDictData('ticket_priority'),
    useDictData('ticket_source')
  ])
  statusDict = s
  priorityDict = p
  sourceDict = so
  try {
    const tree = await categoryApi.tree()
    categoryTree.value = tree.data || []
  } catch {
    // ignore
  }
  load()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
</style>
