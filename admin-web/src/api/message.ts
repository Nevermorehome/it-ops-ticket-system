import { del, get, post, put } from '@/utils/request'

export const messageApi = {
  page: (params: any) => get('/api/message/page', params),
  unreadCount: () => get('/api/message/unread-count'),
  read: (id: any) => put(`/api/message/read/${id}`),
  readAll: () => put('/api/message/read-all'),
  remove: (id: any) => del(`/api/message/${id}`)
}

export const webhookApi = {
  list: (params?: any) => get('/api/message/webhook/list', params),
  create: (data: any) => post('/api/message/webhook', data),
  update: (data: any) => put('/api/message/webhook', data),
  remove: (id: any) => del(`/api/message/webhook/${id}`)
}
