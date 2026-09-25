<template>
  <div class="app-container">
    <el-form class="search-form" :inline="true" @submit.prevent>
      <el-form-item label="账号">
        <el-input v-model="query.username" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable style="width: 110px">
          <el-option label="成功" value="0" />
          <el-option label="失败" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
        <el-button :icon="Delete" @click="clearAll">清空日志</el-button>
      </el-form-item>
    </el-form>
    <div class="table-card">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="username" label="账号" width="130" />
        <el-table-column label="结果" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
              {{ row.status === '0' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ipaddr" label="IP" width="140" />
        <el-table-column prop="loginLocation" label="登录地点" width="150" />
        <el-table-column prop="browser" label="浏览器" min-width="140" show-overflow-tooltip />
        <el-table-column prop="os" label="操作系统" min-width="150" show-overflow-tooltip />
        <el-table-column prop="msg" label="消息" min-width="120" show-overflow-tooltip />
        <el-table-column prop="loginTime" label="登录时间" width="170" />
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
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Search, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logApi } from '@/api/system'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ pageNum: 1, pageSize: 10, username: '', status: '' })

async function load() {
  loading.value = true
  try {
    const res = await logApi.loginPage(query)
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
  await ElMessageBox.confirm('确定清空全部登录日志吗？该操作不可恢复。', '危险操作', { type: 'error' })
  await logApi.clearLogin()
  ElMessage.success('已清空')
  load()
}
onMounted(load)
</script>
