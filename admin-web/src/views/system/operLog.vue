<template>
  <div class="app-container">
    <el-form class="search-form" :inline="true" @submit.prevent>
      <el-form-item label="模块">
        <el-input v-model="query.title" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item label="操作人">
        <el-input v-model="query.operName" clearable style="width: 130px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
        <el-button :icon="Delete" @click="clearAll">清空日志</el-button>
      </el-form-item>
    </el-form>
    <div class="table-card">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="title" label="模块" width="130" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">{{ bizLabel(row.businessType) }}</template>
        </el-table-column>
        <el-table-column prop="requestMethod" label="请求方式" width="90" />
        <el-table-column prop="operName" label="操作人" width="100" />
        <el-table-column prop="operIp" label="IP" width="130" />
        <el-table-column prop="operUrl" label="请求地址" min-width="200" show-overflow-tooltip class-name="mono" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
              {{ row.status === '0' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="100" sortable />
        <el-table-column prop="operTime" label="操作时间" width="170" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="detail = row">详情</el-button>
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

    <el-drawer v-model="drawerVisible" title="操作日志详情" size="520px">
      <el-descriptions v-if="detail" :column="1" border size="small">
        <el-descriptions-item label="模块">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.operName }}（{{ detail.deptName || '—' }}）</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detail.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="方法">
          <span class="mono">{{ detail.method }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="URL">
          <span class="mono">{{ detail.operUrl }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="IP">{{ detail.operIp }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail.costTime }} ms</el-descriptions-item>
        <el-descriptions-item label="请求参数">
          <pre class="json">{{ formatJson(detail.operParam) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果">
          <pre class="json">{{ formatJson(detail.jsonResult) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.errorMsg" label="异常信息">
          <pre class="json error">{{ detail.errorMsg }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Search, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logApi } from '@/api/system'

const BIZ: Record<number, string> = { 0: '其它', 1: '新增', 2: '修改', 3: '删除', 4: '导出', 5: '导入' }
function bizLabel(t: number) {
  return BIZ[t] ?? '其它'
}

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ pageNum: 1, pageSize: 10, title: '', operName: '' })
const detail = ref<any>(null)
const drawerVisible = computed({
  get: () => !!detail.value,
  set: (v) => {
    if (!v) detail.value = null
  }
})

function formatJson(s: string) {
  if (!s) return '—'
  try {
    return JSON.stringify(JSON.parse(s), null, 2)
  } catch {
    return s
  }
}

async function load() {
  loading.value = true
  try {
    const res = await logApi.operationPage(query)
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
async function clearAll() {
  await ElMessageBox.confirm('确定清空全部操作日志吗？该操作不可恢复。', '危险操作', { type: 'error' })
  await logApi.clearOperation()
  ElMessage.success('已清空')
  load()
}
onMounted(load)
</script>

<style scoped>
.json {
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  margin: 0;
  max-height: 220px;
  overflow: auto;
}
.error {
  color: #f56c6c;
}
</style>
