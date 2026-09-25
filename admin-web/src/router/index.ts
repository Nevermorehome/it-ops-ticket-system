import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'
import Layout from '@/layout/index.vue'

/** 静态路由(不需要菜单授权) */
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'ticket/detail/:id',
        name: 'TicketDetail',
        component: () => import('@/views/ticket/detail.vue'),
        meta: { title: '工单详情', hidden: true, activeMenu: '/ticket/list' }
      },
      {
        path: 'ticket/edit',
        name: 'TicketEdit',
        component: () => import('@/views/ticket/edit.vue'),
        meta: { title: '编辑工单', hidden: true, activeMenu: '/ticket/list' }
      },
      {
        path: 'ticket/create',
        name: 'TicketCreate',
        component: () => import('@/views/ticket/edit.vue'),
        meta: { title: '新建工单', hidden: true, activeMenu: '/ticket/list' }
      }
    ]
  }
]

const notFoundRoute: RouteRecordRaw = {
  path: '/:pathMatch(.*)*',
  name: 'NotFound',
  component: () => import('@/views/error/404.vue'),
  meta: { title: '页面不存在' }
}

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 })
})

const WHITE_LIST = ['/login']
let dynamicLoaded = false

router.beforeEach(async (to, _from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - IT运维工单` : 'IT运维工单'
  const store = useUserStore()

  if (!store.token) {
    if (WHITE_LIST.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    }
    return
  }

  if (to.path === '/login') {
    next('/')
    return
  }

  if (!dynamicLoaded) {
    try {
      await store.fetchInfo()
      store.routes.forEach((r) => router.addRoute(r))
      router.addRoute(notFoundRoute)
      dynamicLoaded = true
      next({ ...to, replace: true })
    } catch (e) {
      store.reset()
      dynamicLoaded = false
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    }
    return
  }
  next()
})

export function resetRouterState() {
  dynamicLoaded = false
}

export default router
