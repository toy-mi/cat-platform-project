import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 布局
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

// 前台页面（懒加载或直接导入）
import Home from '@/views/home/Home.vue'
import CatsGallery from '@/views/home/CatsGallery.vue'
import AdoptionProcess from '@/views/home/AdoptionProcess.vue'
import CampaignGallery from '@/views/home/CampaignGallery.vue'
import CommunityGallery from '@/views/home/CommunityGallery.vue'
import About from '@/views/home/About.vue'

// 后台页面（已移动到 admin 目录）
import CatManage from '@/views/admin/CatManage.vue'
import TaskManage from '@/views/admin/TaskManage.vue'
import AdoptionManage from '@/views/admin/AdoptionManage.vue'
import PostManage from '@/views/admin/PostManage.vue'
import CommentManage from '@/views/admin/CommentManage.vue'
import CampaignManage from '@/views/admin/CampaignManage.vue'
import UserManagement from '@/views/admin/UserManagement.vue'
import AnnouncementManage from '@/views/admin/AnnouncementManage.vue'
import Dashboard from '@/views/admin/Dashboard.vue'  // 后面创建

// 其他页面
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import UserProfile from '@/views/UserProfile.vue'
import NotificationList from '@/views/NotificationList.vue'
import CatDetail from '@/views/home/CatDetail.vue'
import CampaignDetail from '@/views/home/CampaignDetail.vue'
import PostDetail from '@/views/community/PostDetail.vue'
import AnnouncementCarousel from '@/views/home/AnnouncementCarousel.vue'
import AnnouncementAll from '@/views/announcement/AnnouncementAll.vue'
import AnnouncementDetail from '@/views/announcement/AnnouncementDetail.vue'
import AdoptionDetail from '@/views/adoption/AdoptionDetail.vue'
import DonationStatistics from '@/views/donation/DonationStatistics.vue'
import CatStatistics from '@/views/CatStatistics.vue'


const routes = [
  // 前台路由（使用 DefaultLayout）
  {
    path: '/',
    component: DefaultLayout,
    children: [
      { path: '', name: 'Home', component: Home },
      { path: 'cats', name: 'CatsGallery', component: CatsGallery },
      { path: 'cats/:id', name: 'CatDetail', component: () => import('@/views/home/CatDetail.vue') },
      { path: 'adoption-process', name: 'AdoptionProcess', component: AdoptionProcess },
      { path: '/donations',name: 'CampaignGallery',component: () => import('@/views/home/CampaignGallery.vue')},
      { path: '/donations/:id',name: 'CampaignDetail',component: () => import('@/views/donation/CampaignDetail.vue')},
      { path: 'community', name: 'CommunityGallery', component: CommunityGallery },
      { path: 'community/post/:id', name: 'PostDetail', component: PostDetail },
      { path: 'announcements', name: 'AnnouncementCarousel', component: () => import('@/views/home/AnnouncementCarousel.vue')},
      { path: 'announcements/all', name: 'AnnouncementAll', component: AnnouncementAll },
      { path: 'announcements/:id', name: 'AnnouncementDetail', component: AnnouncementDetail },
      { path: 'about', name: 'About', component: About },
      { path: 'profile', name: 'UserProfile', component: UserProfile, meta: { requiresAuth: true } },
      { path: 'notifications', name: 'NotificationList', component: NotificationList, meta: { requiresAuth: true } },
      { path: 'adoptions/:id', name: 'AdoptionDetail', component: AdoptionDetail, meta: { requiresAuth: true } },
      { path: '/tasks-square',name: 'TaskSquare',component: () => import('@/views/tasks/TaskSquare.vue')},
      { path: 'tasks/:id', name: 'TaskDetail', component: () => import('@/views/tasks/TaskDetail.vue') },
      { path: 'adoption-process',name: 'AdoptionProcess',component: () => import('@/views/adoption/AdoptionProcess.vue')},
      { path: 'adoption/apply', name: 'AdoptionApply', component: () => import('@/views/adoption/AdoptionApply.vue')},
      { path: 'my-adoptions',  name: 'MyAdoptions',  component: () => import('@/views/adoption/MyAdoptions.vue'),  meta: { requiresAuth: true }},
      { path: '/user/:userId', name: 'UserPublic', component: () => import('@/views/UserPublic.vue'), meta: { requiresAuth: false }  }// 允许未登录用户查看公开信息
      // {  path: '/notifications',name: 'NotificationList',component: NotificationList,meta: { requiresAuth: true }}
    ]
  },
  // 后台路由（使用 AdminLayout，需要管理员权限）
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, roles: ['ADMIN', 'VOLUNTEER'] },
    children: [
      { path: '', redirect: 'cats' },   // 访问 /admin 时重定向到 /admin/cats
      { path: 'cats', name: 'CatManage', component: CatManage },
      { path: 'tasks', name: 'TaskManage', component: TaskManage },
      { path: 'adoptions', name: 'AdoptionManage', component: AdoptionManage },
      { path: 'dashboard', name: 'Dashboard', component: Dashboard ,meta: { roles: ['ADMIN'] } },
      { path: 'community', name: 'CommunityManage', component: () => import('@/views/admin/CommunityManage.vue'), meta: { roles: ['ADMIN'] }  }, // 需创建
      { path: 'campaigns', name: 'CampaignManage', component: CampaignManage , meta: { roles: ['ADMIN'] } },
      { path: 'campaigns/:id',name: 'CampaignDetailAdmin',component: () => import('@/views/admin/CampaignDetail.vue'), meta: { roles: ['ADMIN'] } }, 
      { path: 'announcements', name: 'AnnouncementManage', component: AnnouncementManage, meta: { roles: ['ADMIN'] }  },
      { path: 'users', name: 'UserManagement', component: UserManagement , meta: { roles: ['ADMIN'] } },
      { path: 'notifications', component: () => import('@/views/NotificationList.vue'), meta: { requiresAuth: true } }
    ]
  },
  // 统计页面（可放在前台或后台，暂时放在前台但仅管理员可访问）
  { path: '/statistics', name: 'CatStatistics', component: CatStatistics, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/donation/statistics', name: 'DonationStatistics', component: DonationStatistics, meta: { requiresAuth: true, roles: ['ADMIN'] } },
  // 认证页面
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  // 重定向
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})



// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.roles && !to.meta.roles.includes(userStore.userRole)) {
    next('/')
  } else {
    next()
  }
})

export default router