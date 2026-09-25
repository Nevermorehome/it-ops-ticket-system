import { del, get, post, put } from '@/utils/request'

export const categoryApi = {
  list: (params?: any) => get('/api/base/category/list', params),
  tree: (params?: any) => get('/api/base/category/tree', params),
  create: (data: any) => post('/api/base/category', data),
  update: (data: any) => put('/api/base/category', data),
  remove: (id: any) => del(`/api/base/category/${id}`)
}

export const locationApi = {
  list: (params?: any) => get('/api/base/location/list', params),
  create: (data: any) => post('/api/base/location', data),
  update: (data: any) => put('/api/base/location', data),
  remove: (id: any) => del(`/api/base/location/${id}`)
}

export const assetApi = {
  page: (params: any) => get('/api/base/asset/page', params),
  create: (data: any) => post('/api/base/asset', data),
  update: (data: any) => put('/api/base/asset', data),
  remove: (id: any) => del(`/api/base/asset/${id}`)
}
