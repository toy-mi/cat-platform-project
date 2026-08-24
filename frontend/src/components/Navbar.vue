<template>
  <header class="navbar">
    <div class="container navbar-container">
      <div class="logo" @click="$router.push('/')">
        <span>🐱 猫咪养护平台</span>
      </div>
      <nav class="nav-links">
        <router-link to="/cats">猫咪图鉴</router-link>
        <router-link to="/tasks-square">任务广场</router-link>
        <router-link to="/adoption-process">领养流程</router-link>
        <router-link to="/donations">募捐活动</router-link>
        <router-link to="/community">社区动态</router-link>
        <router-link to="/announcements">公告栏</router-link>
        <router-link to="/about">关于我们</router-link>
      </nav>
      <div class="user-actions">
        <NotificationBell />
        <template v-if="isLoggedIn">
          <el-dropdown @command="handleCommand">
            <span class="user-name">{{ userInfo?.nickname || userInfo?.username || '用户' }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <!-- 普通用户显示“申请成为志愿者” -->
                <el-dropdown-item v-if="!isAdmin && !isVolunteer" command="applyVolunteer">
                  申请成为志愿者
                </el-dropdown-item>
                <!-- 管理员或志愿者显示管理后台入口 -->
                <el-dropdown-item v-if="isAdmin || isVolunteer" command="admin">
                  管理后台
                </el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" size="small" @click="$router.push('/login')">登录</el-button>
          <el-button size="small" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { applyVolunteer } from '@/api/user'
import { ElMessage } from 'element-plus'
import NotificationBell from '@/components/NotificationBell.vue'

const router = useRouter()
const userStore = useUserStore()
const { isLoggedIn, userInfo, isAdmin, isVolunteer } = storeToRefs(userStore)

// 下拉菜单命令处理
const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'admin') {
    // 根据角色跳转到合适的后台页面
  const target = userStore.userRole === 'ADMIN' ? '/admin/dashboard' : '/admin/cats'
  router.push(target)
  } else if (command === 'applyVolunteer') {
    try {
      const res = await applyVolunteer()
      if (res.code === 200) {
        ElMessage.success(res.message)
        // 申请成功后刷新用户信息（重新获取）
        const { getCurrentUser } = await import('@/api/user')
        const userRes = await getCurrentUser()
        if (userRes.code === 200) {
          userStore.setUserInfo(userRes.data)
        }
      } else {
        ElMessage.error(res.message || '申请失败')
      }
    } catch (error) {
      console.error('申请志愿者失败', error)
      ElMessage.error('申请失败，请稍后重试')
    }
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/')
  }
}

// 页面挂载时从 localStorage 恢复用户信息（用于刷新页面保持登录状态）
onMounted(() => {
  if (!userStore.userInfo && localStorage.getItem('token')) {
    userStore.setUserInfo({
      id: localStorage.getItem('userId'),
      username: localStorage.getItem('username'),
      nickname: localStorage.getItem('nickname'),
      role: localStorage.getItem('userRole')
    })
  }
})

// 监听登录状态变化（可用于调试）
watch(isLoggedIn, (newVal) => {
  console.log('Navbar: isLoggedIn changed to', newVal)
}, { immediate: true })
</script>

<style scoped>
.navbar {
  background: white;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}
.navbar-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.logo {
  font-size: 20px;
  font-weight: 600;
  cursor: pointer;
  color: #3b82f6;
}
.nav-links {
  display: flex;
  gap: 2rem;
}
.nav-links a {
  text-decoration: none;
  color: #1e293b;
  font-weight: 500;
  transition: color 0.2s;
}
.nav-links a:hover,
.nav-links a.router-link-active {
  color: #3b82f6;
}
.user-actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}
.user-name {
  cursor: pointer;
  padding: 0 8px;
}
</style>