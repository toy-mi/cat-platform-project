<template>
  <div class="login-container">
    <div class="login-bg"></div>
    <div class="login-wrapper">
      <el-card class="login-card" shadow="hover">
        <div class="login-header">
          <div class="login-icon">🐱</div>
          <h2>开启猫咪之旅吧~</h2>
          <p>登录猫咪养护平台</p>
        </div>
        <el-form :model="form" label-width="0" class="login-form">
          <el-form-item>
            <el-input
              v-model="form.username"
              placeholder="用户名"
              prefix-icon="User"
              size="large"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="handleLogin"
              :loading="loading"
              size="large"
              class="login-btn"
            >
              登录
            </el-button>
            <div class="register-link">
              还没有账号？
              <el-button type="text" @click="$router.push('/register')">立即注册</el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const form = reactive({ username: '', password: '' })
const loading = ref(false)

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await login(form)
    if (res.code === 200) {
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data.userInfo)
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fef7e8 0%, #fff5e6 100%);
  overflow: hidden;
}
.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(circle at 10% 20%, rgba(255,245,220,0.6) 2%, transparent 2.5%);
  background-size: 30px 30px;
  pointer-events: none;
}
.login-wrapper {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 460px;
  padding: 0 20px;
}
.login-card {
  border-radius: 24px;
  border: none;
  box-shadow: 0 20px 35px -12px rgba(0,0,0,0.1);
  transition: transform 0.2s;
}
.login-card:hover {
  transform: translateY(-4px);
}
.login-header {
  text-align: center;
  margin-bottom: 24px;
}
.login-icon {
  font-size: 48px;
  margin-bottom: 12px;
}
.login-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
}
.login-header p {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}
.login-form {
  padding: 0 8px;
}
.login-form :deep(.el-input__wrapper) {
  border-radius: 40px;
  padding: 8px 16px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  transition: all 0.2s;
}
.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #ff9f4a40;
  border-color: #ff9f4a;
}
.login-btn {
  width: 100%;
  border-radius: 40px;
  padding: 12px;
  font-weight: 600;
  font-size: 16px;
  background: linear-gradient(90deg, #ff9f4a 0%, #ffb77c 100%);
  border: none;
  transition: all 0.2s;
}
.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(255,159,74,0.3);
}
.register-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #64748b;
}
.register-link .el-button {
  font-size: 14px;
  color: #ff9f4a;
  padding: 0;
}
.register-link .el-button:hover {
  color: #ff8c2e;
}
</style>