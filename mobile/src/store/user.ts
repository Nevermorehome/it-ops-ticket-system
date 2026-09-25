import { defineStore } from 'pinia'
import { authApi } from '@/api'

interface UserState {
  token: string
  user: any
  roles: string[]
  permissions: string[]
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: uni.getStorageSync('itops_token') || '',
    user: {},
    roles: [],
    permissions: []
  }),
  getters: {
    isAdmin: (s) => s.roles.includes('admin'),
    realName: (s) => s.user.realName || s.user.username || ''
  },
  actions: {
    async login(username: string, password: string) {
      const data = await authApi.login(username, password)
      this.token = data.token
      uni.setStorageSync('itops_token', data.token)
    },
    async fetchInfo() {
      const info = await authApi.info()
      this.user = info.user || {}
      this.roles = info.roles || []
      this.permissions = info.permissions || []
      uni.setStorageSync('itops_user', JSON.stringify(this.user))
      return info
    },
    hasPermi(perm: string) {
      if (this.isAdmin) return true
      return this.permissions.includes(perm)
    },
    async logout() {
      try {
        await authApi.logout()
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
      uni.removeStorageSync('itops_token')
      uni.removeStorageSync('itops_user')
    }
  }
})
