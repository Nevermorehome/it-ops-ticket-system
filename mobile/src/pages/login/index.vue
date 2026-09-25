<template>
  <view class="login">
    <view class="brand">
      <view class="logo">IT</view>
      <text class="title">信息部 IT 运维工单</text>
      <text class="sub">随时随地 · 现场留痕</text>
    </view>

    <view class="form card">
      <view class="item">
        <text class="label">账号</text>
        <input v-model="form.username" class="input" placeholder="请输入账号" />
      </view>
      <view class="item">
        <text class="label">密码</text>
        <input v-model="form.password" class="input" password placeholder="请输入密码" @confirm="onSubmit" />
      </view>
      <button class="submit" :loading="loading" @tap="onSubmit">登 录</button>
      <text class="tip">默认账号 admin / admin123</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function onSubmit() {
  if (!form.username || !form.password) {
    uni.showToast({ title: '请输入账号和密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    await userStore.fetchInfo()
    uni.showToast({ title: '登录成功', icon: 'success' })
    uni.switchTab({ url: '/pages/ticket/list' })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login {
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
  width: 90rpx;
  color: #606266;
}
.input {
  flex: 1;
  height: 56rpx;
}
.submit {
  margin-top: 56rpx;
  background: #2b6cb0;
  color: #fff;
  border-radius: 12rpx;
  font-size: 32rpx;
}
.tip {
  display: block;
  text-align: center;
  margin-top: 24rpx;
  color: #c0c4cc;
  font-size: 24rpx;
}
</style>
