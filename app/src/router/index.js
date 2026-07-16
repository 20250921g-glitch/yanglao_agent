import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import UserLayout from '@/layout/UserLayout.vue'
import Home from '@/views/Home.vue'
import Profile from '@/views/Profile.vue'
import FeaturePlaceholder from '@/views/FeaturePlaceholder.vue'
import HealthArchive from '@/views/HealthArchive.vue'
import Activity from '@/views/Activity.vue'
import Community from '@/views/Community.vue'
import ElderlyService from '@/views/ElderlyService.vue'
import Mall from '@/views/Mall.vue'
import Message from '@/views/Message.vue'

const routes = [
  { path: '/login', name: 'login', component: Login },
  { path: '/register', name: 'register', component: Register },
  {
    path: '/',
    component: UserLayout,
    meta: { auth: true },
    children: [
      { path: '', component: Home, meta: { title: '首页' } },
      { path: 'profile', component: Profile, meta: { title: '个人中心' } },
      { path: 'health', component: HealthArchive, meta: { title: '健康档案' } },
      { path: 'activity', component: Activity, meta: { title: '活动报名' } },
      { path: 'community', component: Community, meta: { title: '邻里圈' } },
      { path: 'service', component: ElderlyService, meta: { title: '养老服务' } },
      { path: 'mall', component: Mall, meta: { title: '商城' } },
      { path: 'message', component: Message, meta: { title: '消息通知' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('app_token')
  const needAuth = to.matched.some(r => r.meta && r.meta.auth)
  if (needAuth && !token) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    // 已登录用户访问登录/注册页，直接进首页
    next('/')
  } else {
    next()
  }
})

export default router
