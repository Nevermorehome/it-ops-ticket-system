import { get, post } from '@/utils/request'

export interface LoginBody {
  username: string
  password: string
}

export function login(data: LoginBody) {
  return post('/api/auth/login', data)
}
export function getUserInfo() {
  return get('/api/auth/info')
}
export function logout() {
  return post('/api/auth/logout')
}
export function changePassword(oldPassword: string, newPassword: string) {
  return post('/api/auth/password', { oldPassword, newPassword })
}
