<script setup lang="ts">
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'
import { hasServerUrl } from '@/config'

onLaunch(() => {
  const userStore = useUserStore()
  if (!userStore.token) {
    // #ifdef APP-PLUS
    // App 端首次启动未配置服务器地址时, 进入配置页
    if (!hasServerUrl()) {
      uni.reLaunch({ url: '/pages/server/index' })
      return
    }
    // #endif
    uni.reLaunch({ url: '/pages/login/index' })
  }
})

onShow(() => {})
</script>

<style lang="scss">
page {
  background-color: #f4f5f7;
  font-size: 28rpx;
  color: #303133;
}

.flex {
  display: flex;
}
.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin: 20rpx;
}
.text-muted {
  color: #909399;
  font-size: 24rpx;
}
.btn-primary {
  background: #2b6cb0;
  color: #fff;
  border-radius: 12rpx;
}
</style>
