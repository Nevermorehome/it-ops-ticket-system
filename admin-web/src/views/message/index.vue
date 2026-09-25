<template>
  <div class="app-container">
    <div class="table-card">
      <div class="toolbar">
        <el-radio-group v-model="query.isRead" @change="onSearch">
          <el-radio-button :value="undefined">全部</el-radio-button>
          <el-radio-button :value="0">未读</el-radio-button>
          <el-radio-button :value="1">已读</el-radio-button>
        </el-radio-group>
        <el-button style="margin-left: 12px" @click="readAll">全部标为已读</el-button>
      </div>
      <el-table v-loading="loading" :data="list" border stripe @row-click="openDetail">
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-badge is-dot :hidden="row.isRead === 1" type="danger">
              <span style="display: inline-block; width: 10px"></span>
            </el-badge>
            <el-tag v-if="row.isRead === 1" type="info" size="small">已读</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180">
          <template #default="{ row }">
            <span :class="{ unread: row.isRead === 0 }">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="messageType" label="类型" width="110" />
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button v-if="row.isRead === 0" link type="primary" size="small" @click.stop="markRead(row)">
              标为已读
            </el-button>
            <el-button link type="danger" size="small" @click.stop="remove(row)">删除</el-button>
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

    <el-drawer v-model="detailVisible" title="消息详情" size="420px">
      <div v-if="current" class="msg-detail">
        <h3>{{ current.title }}</h3>
        <div class="meta">{{ current.createTime }} · {{ current.messageType }}</div>
        <div class="content">{{ current.content }}</div>
        <el-button
          v-if="current.bizType === 'TICKET'"
          type="primary"
          style="margin-top: 16px"
          @click="$router.push(`/ticket/detail/${current.bizId}`)"
        >
          查看关联工单
        </el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { messageApi } from '@/api/message'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const query = reactive<any>({ pageNum: 1, pageSize: 10, isRead: undefined })
const detailVisible = ref(false)
const current = ref<any>(null)

async function load() {
  loading.value = true
  try {
    const params: any = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.isRead !== undefined && query.isRead !== null && query.isRead !== '') {
      params.isRead = query.isRead
    }
    const res = await messageApi.page(params)
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
async function openDetail(row: any) {
  current.value = row
  detailVisible.value = true
  if (row.isRead === 0) {
    await markRead(row, false)
  }
}
async function markRead(row: any, reload = true) {
  await messageApi.read(row.messageId)
  row.isRead = 1
  if (reload) load()
}
async function readAll() {
  await messageApi.readAll()
  ElMessage.success('已全部标为已读')
  load()
}
async function remove(row: any) {
  await ElMessageBox.confirm('确定删除该消息吗？', '提示', { type: 'warning' })
  await messageApi.remove(row.messageId)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.unread {
  font-weight: 600;
}
.msg-detail h3 {
  margin: 0 0 8px;
}
.msg-detail .meta {
  color: #909399;
  font-size: 12px;
  margin-bottom: 14px;
}
.msg-detail .content {
  background: #f5f7fa;
  padding: 14px;
  border-radius: 6px;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
