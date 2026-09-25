import { request, uploadFile } from '@/utils/request'

// ---------------- 认证 ----------------
export const authApi = {
  login: (username: string, password: string) =>
    request<string>({ url: '/auth/login', method: 'POST', data: { username, password } }),
  info: () => request<any>({ url: '/auth/info' }),
  logout: () => request({ url: '/auth/logout', method: 'POST' }),
  changePassword: (oldPassword: string, newPassword: string) =>
    request({ url: '/auth/password', method: 'POST', data: { oldPassword, newPassword } })
}

// ---------------- 工单 ----------------
export const ticketApi = {
  page: (params: any) => request<any>({ url: '/ticket/page', data: params }),
  detail: (id: string | number) => request<any>({ url: `/ticket/${id}` }),
  create: (ticket: any, attachments: any[]) =>
    request({ url: '/ticket', method: 'POST', data: { ticket, attachments } }),
  edit: (ticket: any) => request({ url: '/ticket', method: 'PUT', data: ticket }),
  action: (action: string, data: any) => request({ url: `/ticket/${action}`, method: 'PUT', data }),
  addFieldRecord: (id: string | number, record: any, images: any[]) =>
    request({ url: `/ticket/${id}/field-record`, method: 'POST', data: { record, images } }),
  userOptions: (keyword?: string) => request<any[]>({ url: '/system/user/options', data: { keyword } })
}

// ---------------- 基础数据 ----------------
export const baseApi = {
  categoryTree: () => request<any[]>({ url: '/base/category/tree' }),
  locationList: () => request<any[]>({ url: '/base/location/list' }),
  assetPage: (params: any) => request<any>({ url: '/base/asset/page', data: params }),
  deptTree: () => request<any[]>({ url: '/system/dept/tree' })
}

// ---------------- 字典 ----------------
export const dictApi = {
  byType: (dictType: string) => request<any[]>({ url: `/system/dict/data/type/${dictType}` })
}

// ---------------- 消息 ----------------
export const messageApi = {
  page: (params: any) => request<any>({ url: '/message/page', data: params }),
  unreadCount: () => request<number>({ url: '/message/unread-count' }),
  read: (id: string | number) => request({ url: `/message/read/${id}`, method: 'PUT' }),
  readAll: () => request({ url: '/message/read-all', method: 'PUT' })
}

export { uploadFile }
