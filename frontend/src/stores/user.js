import { defineStore } from 'pinia'

// 用户状态管理
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: localStorage.getItem('userId') || '',
    userRole: localStorage.getItem('userRole') || '',
    userInfo: null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.userRole === 'ADMIN',
    isVolunteer: (state) => state.userRole === 'VOLUNTEER'
  },
  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },
    setUserInfo(info) {
  this.userInfo = info
  this.userId = info.id
  this.userRole = info.role
  localStorage.setItem('userId', info.id)
  localStorage.setItem('userRole', info.role)
  localStorage.setItem('username', info.username)      // 新增
  localStorage.setItem('nickname', info.nickname || '') // 新增
},
    logout() {
      this.token = ''
      this.userId = ''
      this.userRole = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('userRole')
    }
  }
})