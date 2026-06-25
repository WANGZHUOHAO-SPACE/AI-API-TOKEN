import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/views/auth/LoginView.vue'), meta: { public: true } },
    { path: '/console-login', component: () => import('@/views/auth/ConsoleLoginView.vue'), meta: { guest: true } },
    { path: '/register', component: () => import('@/views/auth/RegisterView.vue'), meta: { guest: true } },
    {
      path: '/',
      component: () => import('@/layouts/AppLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/login' },
        { path: 'dashboard', component: () => import('@/views/DashboardView.vue'), meta: { title: '首页仪表盘' } },
        { path: 'keys', component: () => import('@/views/ApiKeysView.vue'), meta: { title: 'API Key 管理' } },
        { path: 'playground', component: () => import('@/views/ChatPlaygroundView.vue'), meta: { title: 'AI 调用测试' } },
        { path: 'logs', component: () => import('@/views/LogsView.vue'), meta: { title: '调用日志' } },
        { path: 'models', component: () => import('@/views/ModelMarketView.vue'), meta: { title: '模型广场', public: true } },
        { path: 'contact', component: () => import('@/views/ContactView.vue'), meta: { title: '联系我们', userOnly: true } },
        { path: 'admin/users', component: () => import('@/views/admin/UsersView.vue'), meta: { title: '用户管理', admin: true } },
        { path: 'admin/recharges', component: () => import('@/views/admin/RechargeOrdersView.vue'), meta: { title: '充值订单', admin: true } },
        { path: 'admin/logs', component: () => import('@/views/admin/AdminLogsView.vue'), meta: { title: '全站日志', admin: true } },
      ],
    },
    { path: '/:pathMatch(.*)*', component: () => import('@/views/NotFoundView.vue') },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !to.meta.public && !auth.isLoggedIn) {
    return { path: '/console-login', query: { redirect: to.fullPath } }
  }
  if (auth.isLoggedIn && !auth.user && !to.meta.public && !to.meta.guest) {
    try {
      await auth.fetchProfile()
    } catch {
      return { path: '/console-login', query: { redirect: to.fullPath } }
    }
  }
  if (to.meta.admin && !auth.isAdmin) return '/dashboard'
  if (to.meta.userOnly && auth.isAdmin) return '/dashboard'
  if (to.meta.guest && auth.isLoggedIn && auth.user) return '/dashboard'
  return true
})

export default router
