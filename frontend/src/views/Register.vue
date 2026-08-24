<template>
  <div class="register-container">
    <div class="register-bg"></div>
    <div class="register-wrapper">
      <el-card class="register-card" shadow="hover">
        <div class="register-header">
          <div class="register-icon">🐱</div>
          <h2>加入我们</h2>
          <p>注册成为猫咪养护平台的一员</p>
        </div>
        <el-form :model="form" :rules="rules" ref="formRef" label-width="0" class="register-form">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名（必填）"
              prefix-icon="User"
              size="large"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码（必填）"
              prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="form.nickname"
              placeholder="昵称（选填）"
              prefix-icon="Star"
              size="large"
              clearable
            />
          </el-form-item>
          <el-form-item prop="email">
            <el-input
              v-model="form.email"
              placeholder="邮箱（选填）"
              prefix-icon="Message"
              size="large"
              clearable
            />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="手机号（选填）"
              prefix-icon="Phone"
              size="large"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="handleRegister"
              :loading="loading"
              size="large"
              class="register-btn"
            >
              注册
            </el-button>
            <div class="login-link">
              已有账号？
              <el-button type="text" @click="$router.push('/login')">立即登录</el-button>
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
import { register } from '@/api/user'

const router = useRouter()
const form = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: ''
})
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确邮箱', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确手机号', trigger: 'blur' }]
}
const formRef = ref()
const loading = ref(false)

const handleRegister = async () => {
  await formRef.value?.validate()
  loading.value = true
  try {
    const res = await register(form)
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fef7e8 0%, #fff5e6 100%);
  overflow: hidden;
  padding: 20px;
}
.register-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(circle at 10% 20%, rgba(255,245,220,0.6) 2%, transparent 2.5%);
  background-size: 30px 30px;
  pointer-events: none;
}
.register-wrapper {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 520px;
}
.register-card {
  border-radius: 24px;
  border: none;
  box-shadow: 0 20px 35px -12px rgba(0,0,0,0.1);
  transition: transform 0.2s;
}
.register-card:hover {
  transform: translateY(-4px);
}
.register-header {
  text-align: center;
  margin-bottom: 24px;
}
.register-icon {
  font-size: 48px;
  margin-bottom: 12px;
}
.register-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
}
.register-header p {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}
.register-form {
  padding: 0 8px;
}
.register-form :deep(.el-input__wrapper) {
  border-radius: 40px;
  padding: 8px 16px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  transition: all 0.2s;
}
.register-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.register-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #ff9f4a40;
  border-color: #ff9f4a;
}
.register-btn {
  width: 100%;
  border-radius: 40px;
  padding: 12px;
  font-weight: 600;
  font-size: 16px;
  background: linear-gradient(90deg, #ff9f4a 0%, #ffb77c 100%);
  border: none;
  transition: all 0.2s;
}
.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(255,159,74,0.3);
}
.login-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #64748b;
}
.login-link .el-button {
  font-size: 14px;
  color: #ff9f4a;
  padding: 0;
}
.login-link .el-button:hover {
  color: #ff8c2e;
}
</style>