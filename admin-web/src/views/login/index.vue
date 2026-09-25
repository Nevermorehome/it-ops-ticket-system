<template>
  <div class="login-page">
    <div class="login-box">
      <div class="brand">
        <el-icon :size="40" color="#409eff"><Monitor /></el-icon>
        <h1>信息部 IT 运维工单系统</h1>
        <p>工单记录 · 现场留痕 · 统计报表</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent>
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-button type="primary" class="login-btn" :loading="loading" @click="onSubmit">
          登 录
        </el-button>
      </el-form>
      <div class="tip">默认管理员：admin / admin123</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { resetRouterState } from '@/router'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    resetRouterState()
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/'
    router.replace(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f3a68 0%, #2b6cb0 50%, #409eff 100%);
}
.login-box {
  width: 400px;
  background: #fff;
  border-radius: 10px;
  padding: 40px 36px 30px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
}
.brand {
  text-align: center;
  margin-bottom: 26px;
}
.brand h1 {
  font-size: 20px;
  margin: 12px 0 6px;
  color: #1f2d3d;
}
.brand p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}
.login-btn {
  width: 100%;
}
.tip {
  text-align: center;
  color: #c0c4cc;
  font-size: 12px;
  margin-top: 14px;
}
</style>
