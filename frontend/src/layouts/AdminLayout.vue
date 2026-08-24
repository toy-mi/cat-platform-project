<template>
  <el-container class="admin-layout">
    <el-aside width="200px" class="admin-sidebar">
      <div class="logo">猫咪管理后台</div>
      <el-menu
        router
        :default-active="$route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <!-- 仪表盘（志愿者和管理员都有） -->
        <el-menu-item v-if="isAdmin" index="/admin/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>

        <!-- 猫咪管理（志愿者可看） -->
        <el-menu-item v-if="isVolunteer || isAdmin" index="/admin/cats">
          <el-icon><Menu /></el-icon>
          <span>猫咪管理</span>
        </el-menu-item>

        <!-- 任务管理（志愿者可看） -->
        <el-menu-item v-if="isVolunteer || isAdmin" index="/admin/tasks">
          <el-icon><List /></el-icon>
          <span>任务管理</span>
        </el-menu-item>

        <!-- 领养管理（志愿者可看） -->
        <el-menu-item v-if="isVolunteer || isAdmin" index="/admin/adoptions">
          <el-icon><Document /></el-icon>
          <span>领养管理</span>
        </el-menu-item>

        <!-- 社区管理（仅管理员） -->
        <el-menu-item v-if="isAdmin" index="/admin/community">
          <el-icon><ChatDotRound /></el-icon>
          <span>社区管理</span>
        </el-menu-item>

        <!-- 募捐管理（仅管理员） -->
        <el-menu-item v-if="isAdmin" index="/admin/campaigns">
          <el-icon><Money /></el-icon>
          <span>募捐管理</span>
        </el-menu-item>

        <!-- 公告管理（仅管理员） -->
        <el-menu-item v-if="isAdmin" index="/admin/announcements">
          <el-icon><Bell /></el-icon>
          <span>公告管理</span>
        </el-menu-item>

        <!-- 用户管理（仅管理员） -->
        <el-menu-item v-if="isAdmin" index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <!-- 新增：站内信（所有登录后台的用户都可查看） -->
        <el-menu-item index="/admin/notifications">
          <el-icon><Bell /></el-icon>
          <span>站内信</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <div class="header-right">
          <!-- 新增铃铛组件 -->
          <NotificationBell />
          <el-button type="text" @click="goToFront" class="front-btn">
            <el-icon><House /></el-icon> 回到前台
          </el-button>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ userStore.userInfo?.username || '管理员' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import {
  DataLine, Menu, List, Document, ChatDotRound, Money, Bell, User, ArrowDown, House
} from '@element-plus/icons-vue'
import NotificationBell from '@/components/NotificationBell.vue'

const router = useRouter()
const userStore = useUserStore()
const { isAdmin, isVolunteer } = storeToRefs(userStore)

const goToFront = () => {
  router.push('/')
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}
.admin-sidebar {
  background-color: #f5f7fa;   /* 浅灰背景 */
  color: #2c3e50;
  border-right: 1px solid #e4e7ed;
}
.admin-sidebar .logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
}
.admin-header {
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 20px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.front-btn {
  color: #409eff;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #2c3e50;
}
/* 覆盖 Element Plus 菜单默认样式 */
:deep(.el-menu) {
  border-right: none;
  background-color: #f5f7fa;
}
:deep(.el-menu-item) {
  color: #5a6874;
  background-color: #f5f7fa;
  transition: all 0.2s;
}
:deep(.el-menu-item:hover) {
  background-color: #ecf0f3;
  color: #409eff;
}
:deep(.el-menu-item.is-active) {
  color: #409eff;
  background-color: #ecf0f3;
  border-right: 2px solid #409eff;
}
:deep(.el-menu-item .el-icon) {
  color: #5a6874;
}
:deep(.el-menu-item.is-active .el-icon) {
  color: #409eff;
}
</style>