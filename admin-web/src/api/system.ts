import { del, get, post, put } from '@/utils/request'

// ---------------- 用户 ----------------
export const userApi = {
  page: (params: any) => get('/api/system/user/page', params),
  options: (keyword?: string) => get('/api/system/user/options', { keyword }),
  detail: (id: any) => get(`/api/system/user/${id}`),
  create: (user: any, roleIds: any[]) => post('/api/system/user', { user, roleIds }),
  update: (user: any, roleIds: any[]) => put('/api/system/user', { user, roleIds }),
  remove: (ids: any) => del(`/api/system/user/${ids}`),
  resetPwd: (userId: any, newPassword: string) =>
    put('/api/system/user/reset-pwd', { userId, newPassword })
}

// ---------------- 角色 ----------------
export const roleApi = {
  page: (params: any) => get('/api/system/role/page', params),
  all: () => get('/api/system/role/all'),
  detail: (id: any) => get(`/api/system/role/${id}`),
  create: (data: any) => post('/api/system/role', data),
  update: (data: any) => put('/api/system/role', data),
  remove: (id: any) => del(`/api/system/role/${id}`)
}

// ---------------- 菜单 ----------------
export const menuApi = {
  list: (params?: any) => get('/api/system/menu/list', params),
  tree: (params?: any) => get('/api/system/menu/tree', params),
  create: (data: any) => post('/api/system/menu', data),
  update: (data: any) => put('/api/system/menu', data),
  remove: (id: any) => del(`/api/system/menu/${id}`)
}

// ---------------- 部门 ----------------
export const deptApi = {
  list: (params?: any) => get('/api/system/dept/list', params),
  tree: (params?: any) => get('/api/system/dept/tree', params),
  create: (data: any) => post('/api/system/dept', data),
  update: (data: any) => put('/api/system/dept', data),
  remove: (id: any) => del(`/api/system/dept/${id}`)
}

// ---------------- 字典 ----------------
export const dictApi = {
  typeList: (params?: any) => get('/api/system/dict/type/list', params),
  typeCreate: (data: any) => post('/api/system/dict/type', data),
  typeUpdate: (data: any) => put('/api/system/dict/type', data),
  typeRemove: (id: any) => del(`/api/system/dict/type/${id}`),
  dataList: (params?: any) => get('/api/system/dict/data/list', params),
  dataByType: (dictType: string) => get(`/api/system/dict/data/type/${dictType}`),
  dataCreate: (data: any) => post('/api/system/dict/data', data),
  dataUpdate: (data: any) => put('/api/system/dict/data', data),
  dataRemove: (id: any) => del(`/api/system/dict/data/${id}`)
}

// ---------------- 参数配置 ----------------
export const configApi = {
  list: (params?: any) => get('/api/system/config/list', params),
  value: (key: string) => get(`/api/system/config/value/${key}`),
  create: (data: any) => post('/api/system/config', data),
  update: (data: any) => put('/api/system/config', data),
  remove: (id: any) => del(`/api/system/config/${id}`)
}

// ---------------- 日志 ----------------
export const logApi = {
  loginPage: (params: any) => get('/api/system/log/login/page', params),
  clearLogin: () => del('/api/system/log/login/clear'),
  operationPage: (params: any) => get('/api/system/log/operation/page', params),
  clearOperation: () => del('/api/system/log/operation/clear')
}
