<template>
  <view class="page">
    <!-- 维度切换 -->
    <view class="dim-bar">
      <view
        v-for="d in dimensions"
        :key="d.value"
        class="dim-item"
        :class="{ active: query.dimension === d.value }"
        @tap="switchDimension(d.value)"
      >
        {{ d.label }}
      </view>
    </view>

    <!-- 状态筛选 -->
    <scroll-view scroll-x class="status-bar">
      <view class="status-inner">
        <view class="status-chip" :class="{ active: !query.status }" @tap="switchStatus('')">全部</view>
        <view
          v-for="s in statusDict"
          :key="s.dictValue"
          class="status-chip"
          :class="{ active: query.status === s.dictValue }"
          @tap="switchStatus(s.dictValue)"
        >
          {{ s.dictLabel }}
        </view>
      </view>
    </scroll-view>

    <!-- 工单卡片 -->
    <view class="list">
      <view v-for="t in list" :key="t.ticketId" class="ticket-card" @tap="goDetail(t.ticketId)">
        <view class="row1">
          <text class="no">{{ t.ticketNo }}</text>
          <text class="status" :style="{ background: statusColor(t.status) }">{{ statusLabel(t.status) }}</text>
        </view>
        <view class="title">{{ t.title }}</view>
        <view class="row2">
          <text class="tag priority">{{ priorityLabel(t.priority) }}</text>
          <text class="muted">{{ t.categoryName || '未分类' }}</text>
        </view>
        <view class="row3">
          <text class="muted">{{ t.reporterName }} · {{ t.handlerName ? '处理人:' + t.handlerName : '待指派' }}</text>
          <text class="muted time">{{ t.createTime }}</text>
        </view>
      </view>

      <view v-if="!loading && list.length === 0" class="empty">
        <text class="muted">暂无工单</text>
      </view>
      <view v-if="list.length > 0" class="loadmore">
        <text class="muted">{{ noMore ? '没有更多了' : '上拉加载更多' }}</text>
      </view>
    </view>

    <!-- 新建浮动按钮 -->
    <view class="fab" @tap="goCreate">
      <text class="fab-text">＋ 报单</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { ticketApi } from '@/api'
import { loadDict, statusColor, type DictItem } from '@/utils/dict'

const dimensions = [
  { label: '全部', value: 'all' },
  { label: '我报修的', value: 'created' },
  { label: '待我处理', value: 'assigned' },
  { label: '我协同的', value: 'collaborator' }
]

const query = reactive<any>({
  dimension: 'all',
  status: '',
  pageNum: 1,
  pageSize: 10
})
const list = ref<any[]>([])
const loading = ref(false)
const noMore = ref(false)
let statusDict: DictItem[] = []
let priorityDict: DictItem[] = []

function statusLabel(s: string) {
  return statusDict.find((d) => d.dictValue === s)?.dictLabel || s
}
function priorityLabel(p: number) {
  return priorityDict.find((d) => Number(d.dictValue) === p)?.dictLabel || ''
}

async function load(reset = false) {
  if (loading.value) return
  if (reset) {
    query.pageNum = 1
    noMore.value = false
  }
  loading.value = true
  try {
    const res = await ticketApi.page(query)
    const records = res.records || []
    list.value = reset ? records : [...list.value, ...records]
    if (records.length < query.pageSize) noMore.value = true
  } finally {
    loading.value = false
  }
}

function switchDimension(v: string) {
  if (query.dimension === v) return
  query.dimension = v
  load(true)
}
function switchStatus(v: string) {
  query.status = v
  load(true)
}
function goDetail(id: string | number) {
  uni.navigateTo({ url: `/pages/ticket/detail?id=${id}` })
}
function goCreate() {
  uni.navigateTo({ url: '/pages/ticket/create' })
}

onPullDownRefresh(async () => {
  await load(true)
  uni.stopPullDownRefresh()
})
onReachBottom(() => {
  if (!noMore.value && !loading.value) {
    query.pageNum += 1
    load()
  }
})

onMounted(async () => {
  statusDict = await loadDict('ticket_status')
  priorityDict = await loadDict('ticket_priority')
  load(true)
})
</script>

<style lang="scss" scoped>
.page {
  padding-bottom: 140rpx;
}
.dim-bar {
  display: flex;
  background: #fff;
  padding: 0 10rpx;
  position: sticky;
  top: 0;
  z-index: 10;
}
.dim-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #606266;
  position: relative;
}
.dim-item.active {
  color: #2b6cb0;
  font-weight: 600;
}
.dim-item.active::after {
  content: '';
  position: absolute;
  bottom: 8rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 48rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background: #2b6cb0;
}
.status-bar {
  white-space: nowrap;
  background: #fff;
  padding: 12rpx 20rpx;
  border-top: 1rpx solid #f2f3f5;
}
.status-inner {
  display: inline-flex;
  gap: 16rpx;
}
.status-chip {
  display: inline-block;
  padding: 8rpx 24rpx;
  border-radius: 28rpx;
  background: #f4f5f7;
  color: #606266;
  font-size: 24rpx;
}
.status-chip.active {
  background: rgba(43, 108, 176, 0.12);
  color: #2b6cb0;
}
.ticket-card {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}
.row1 {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.no {
  font-size: 24rpx;
  color: #909399;
}
.status {
  color: #fff;
  font-size: 22rpx;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}
.title {
  font-size: 32rpx;
  font-weight: 600;
  margin: 14rpx 0;
  line-height: 1.4;
}
.row2 {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}
.tag {
  font-size: 22rpx;
  padding: 2rpx 14rpx;
  border-radius: 8rpx;
  background: #fdf6ec;
  color: #e6a23c;
}
.row3 {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.muted {
  color: #909399;
  font-size: 24rpx;
}
.time {
  font-size: 22rpx;
}
.empty,
.loadmore {
  text-align: center;
  padding: 60rpx 0;
}
.fab {
  position: fixed;
  right: 40rpx;
  bottom: 60rpx;
  background: #2b6cb0;
  color: #fff;
  padding: 24rpx 36rpx;
  border-radius: 48rpx;
  box-shadow: 0 8rpx 24rpx rgba(43, 108, 176, 0.4);
  z-index: 99;
}
.fab-text {
  font-size: 30rpx;
}
</style>
