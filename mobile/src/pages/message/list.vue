<template>
  <view class="page">
    <view class="filter-bar">
      <view class="chip" :class="{ active: filter === '' }" @tap="switchFilter('')">全部</view>
      <view class="chip" :class="{ active: filter === '0' }" @tap="switchFilter('0')">未读</view>
      <view class="chip" :class="{ active: filter === '1' }" @tap="switchFilter('1')">已读</view>
      <view class="read-all" @tap="readAll">全部已读</view>
    </view>

    <view v-for="m in list" :key="m.messageId" class="msg-card" @tap="open(m)">
      <view class="row1">
        <view class="dot" v-if="m.isRead === 0"></view>
        <text class="title" :class="{ unread: m.isRead === 0 }">{{ m.title }}</text>
        <text class="time">{{ m.createTime }}</text>
      </view>
      <view class="content">{{ m.content }}</view>
    </view>

    <view v-if="!loading && list.length === 0" class="empty">
      <text class="muted">暂无消息</text>
    </view>
    <view v-if="list.length > 0" class="loadmore">
      <text class="muted">{{ noMore ? '没有更多了' : '上拉加载更多' }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onHide, onReachBottom, onShow } from '@dcloudio/uni-app'
import { messageApi } from '@/api'

let badgeTimer: any

const list = ref<any[]>([])
const loading = ref(false)
const noMore = ref(false)
const filter = ref('')
const query = reactive<any>({ pageNum: 1, pageSize: 10 })

async function load(reset = false) {
  if (loading.value) return
  if (reset) {
    query.pageNum = 1
    noMore.value = false
  }
  loading.value = true
  try {
    const params: any = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (filter.value !== '') params.isRead = Number(filter.value)
    const res = await messageApi.page(params)
    const records = res.records || []
    list.value = reset ? records : [...list.value, ...records]
    if (records.length < query.pageSize) noMore.value = true
  } finally {
    loading.value = false
  }
}

function switchFilter(v: string) {
  filter.value = v
  load(true)
}

async function open(m: any) {
  if (m.isRead === 0) {
    await messageApi.read(m.messageId)
    m.isRead = 1
    refreshBadge()
  }
  if (m.bizType === 'TICKET' && m.bizId) {
    uni.navigateTo({ url: `/pages/ticket/detail?id=${m.bizId}` })
  }
}

async function readAll() {
  await messageApi.readAll()
  uni.showToast({ title: '已全部已读', icon: 'success' })
  load(true)
  refreshBadge()
}

async function refreshBadge() {
  try {
    const count = Number(await messageApi.unreadCount()) || 0
    if (count > 0) {
      uni.setTabBarBadge({ index: 1, text: count > 99 ? '99+' : String(count) })
    } else {
      uni.removeTabBarBadge({ index: 1 })
    }
  } catch {
    // ignore
  }
}

onShow(() => {
  if (uni.getStorageSync('itops_token')) {
    load(true)
    refreshBadge()
    badgeTimer = setInterval(refreshBadge, 60000)
  }
})
onHide(() => {
  if (badgeTimer) clearInterval(badgeTimer)
})
onReachBottom(() => {
  if (!noMore.value && !loading.value) {
    query.pageNum += 1
    load()
  }
})
</script>

<style lang="scss" scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx;
}
.chip {
  padding: 8rpx 28rpx;
  border-radius: 28rpx;
  background: #fff;
  color: #606266;
  font-size: 26rpx;
}
.chip.active {
  background: #2b6cb0;
  color: #fff;
}
.read-all {
  margin-left: auto;
  color: #2b6cb0;
  font-size: 26rpx;
}
.msg-card {
  background: #fff;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}
.row1 {
  display: flex;
  align-items: center;
}
.dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #f56c6c;
  margin-right: 12rpx;
}
.title {
  flex: 1;
  font-size: 30rpx;
}
.title.unread {
  font-weight: 600;
}
.time {
  color: #c0c4cc;
  font-size: 22rpx;
}
.content {
  color: #606266;
  font-size: 26rpx;
  margin-top: 12rpx;
  line-height: 1.6;
}
.empty,
.loadmore {
  text-align: center;
  padding: 60rpx 0;
}
.muted {
  color: #909399;
  font-size: 24rpx;
}
</style>
