<template>
  <view class="page">
    <view class="brand">
      <view class="logo">IT</view>
      <text class="title">服务器配置</text>
      <text class="sub">连接到您的 IT 运维工单后台</text>
    </view>

    <view class="form card">
      <view class="item">
        <text class="label">服务器地址</text>
        <input v-model="url" class="input" placeholder="http://ip:port/api" />
      </view>
      <view class="hint">示例：http://10.101.208.157:8080/api</view>
      <view class="btns">
        <button class="btn-test" :loading="testing" @tap="onTest">测试连接</button>
        <button class="btn-save" :loading="saving" @tap="onSave">保存并登录</button>
      </view>
      <view class="btns">
        <button class="btn-reset" @tap="onReset">恢复默认</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { clearBaseUrl, getDefaultAppUrl, getBaseUrl, setBaseUrl } from '@/config'

const url = ref('')
const testing = ref(false)
const saving = ref(false)

onLoad(() => {
  url.value = getBaseUrl() || getDefaultAppUrl()
})

function validate(): boolean {
  if (!url.value) {
    uni.showToast({ title: '请输入服务器地址', icon: 'none' })
    return false
  }
  if (!/^https?:\/\/.+/.test(url.value)) {
    uni.showToast({ title: '地址需以 http:// 或 https:// 开头', icon: 'none' })
    return false
  }
  // 去除末尾斜杠，避免 //file/upload
  url.value = url.value.replace(/\/+$/, '')
  return true
}

async function onTest() {
  if (!validate()) return
  testing.value = true
  try {
    await new Promise<void>((resolve, reject) => {
      uni.request({
        url: url.value + '/auth/login',
        method: 'POST',
        data: {},
        timeout: 8000,
        success: (res) => {
          // 任意 HTTP 响应都说明服务器可达
          // 业务码可能是 400（参数错误）/401，但都说明后端在线
          if (res.statusCode >= 200 && res.statusCode < 500) {
            resolve()
          } else {
            reject(new Error(`服务器响应异常 ${res.statusCode}`))
          }
        },
        fail: () => reject(new Error('无法连接到服务器'))
      })
    })
    uni.showToast({ title: '连接成功', icon: 'success' })
  } catch (e: any) {
    uni.showToast({ title: e?.message || '连接失败', icon: 'none' })
  } finally {
    testing.value = false
  }
}

async function onSave() {
  if (!validate()) return
  saving.value = true
  try {
    setBaseUrl(url.value)
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/login/index' })
    }, 500)
  } finally {
    saving.value = false
  }
}

function onReset() {
  url.value = getDefaultAppUrl()
  clearBaseUrl()
  uni.showToast({ title: '已恢复默认地址', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(160deg, #2b6cb0 0%, #1f3a68 60%, #16294a 100%);
  padding: 0 48rpx;
}
.brand {
  padding-top: 160rpx;
  text-align: center;
  color: #fff;
}
.logo {
  width: 120rpx;
  height: 120rpx;
  line-height: 120rpx;
  margin: 0 auto 24rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.18);
  font-size: 48rpx;
  font-weight: 700;
}
.title {
  display: block;
  font-size: 40rpx;
  font-weight: 600;
}
.sub {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  opacity: 0.8;
}
.form {
  margin-top: 80rpx;
  padding: 48rpx 40rpx;
}
.item {
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid #ebeef5;
  padding: 24rpx 0;
}
.label {
  width: 180rpx;
  color: #606266;
  flex-shrink: 0;
}
.input {
  flex: 1;
  height: 56rpx;
}
.hint {
  margin-top: 16rpx;
  color: #909399;
  font-size: 24rpx;
}
.btns {
  display: flex;
  gap: 20rpx;
  margin-top: 32rpx;
}
.btn-test,
.btn-save,
.btn-reset {
  flex: 1;
  border-radius: 12rpx;
  font-size: 30rpx;
}
.btn-test {
  background: #f4f5f7;
  color: #2b6cb0;
}
.btn-test::after {
  border: none;
}
.btn-save {
  background: #2b6cb0;
  color: #fff;
}
.btn-save::after {
  border: none;
}
.btn-reset {
  background: #fff;
  color: #606266;
  border: 1rpx solid #dcdfe6;
}
.btn-reset::after {
  border: none;
}
</style>
