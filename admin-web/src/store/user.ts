import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo, logout as logoutApi } from '@/api/auth'
import type { RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/index.vue'

const modules = import.meta.glob('@/views/**/*.vue')

interface UserState {
  token: string
  user: any
  roles: string[]
  permissions: string[]
  menus: any[]
  routes: RouteRecordRaw[]
}

function loadView(component?: string) {
  if (!component) return undefined
  const key = `/src/views/${component}.vue`
  return modules[key] || modules[`${component}.vue`]
}

/** 菜单树 → 路由 */
export function buildRoutes(menus: any[]): RouteRecordRaw[] {
  const routes: RouteRecordRaw[] = []
  menus
    .filter((m) => m.menuType === 'M' || m.menuType === 'C')
    .sort((a, b) => (a.orderNum || 0) - (b.orderNum || 0))
    .forEach((m) => {
      if (m.menuType === 'M') {
        const children = (m.children || [])
          .filter((c: any) => c.menuType === 'C')
          .sort((a: any, b: any) => (a.orderNum || 0) - (b.orderNum || 0))
          .map((c: any) => ({
            path: (c.path || '').replace(/^\//, ''),
            name: `${m.path}-${c.path}`,
            component: loadView(c.component),
            meta: { title: c.menuName, icon: c.icon, perms: c.perms }
          }))
          .filter((c: any) => c.component)
        routes.push({
          path: m.path,
          component: Layout,
          redirect: children.length ? `${m.path}/${children[0].path}` : undefined,
          meta: { title: m.menuName, icon: m.icon },
          children
        } as RouteRecordRaw)
      } else {
        const view = loadView(m.component)
        if (!view) return
        routes.push({
          path: '/',
          component: Layout,
          children: [
            {
              path: (m.path || '').replace(/^\//, ''),
              name: `top-${m.path}`,
              component: view,
              meta: { title: m.menuName, icon: m.icon, perms: m.perms }
            }
          ]
        } as RouteRecordRaw)
      }
    })
  return routes
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('itops_token') || '',
    user: {},
    roles: [],
    permissions: [],
    menus: [],
    routes: []
  }),
  getters: {
    isAdmin: (s) => s.roles.includes('admin')
  },
  actions: {
    async login(username: string, password: string) {
      const res = await loginApi({ username, password })
      this.token = res.data.token
      localStorage.setItem('itops_token', res.data.token)
    },
    async fetchInfo() {
      const res = await getUserInfo()
      const info = res.data
      this.user = info.user || {}
      this.roles = info.roles || []
      this.permissions = info.permissions || []
      this.menus = info.menus || []
      this.routes = buildRoutes(this.menus)
      return info
    },
    hasPermi(perm: string) {
      if (this.isAdmin) return true
      if (!perm) return true
      return this.permissions.includes(perm)
    },
    async logout() {
      try {
        await logoutApi()
      } catch {
        // ignore
      }
      this.reset()
    },
    reset() {
      this.token = ''
      this.user = {}
      this.roles = []
      this.permissions = []
      this.menus = []
      this.routes = []
      localStorage.removeItem('itops_token')
    }
  }
})
