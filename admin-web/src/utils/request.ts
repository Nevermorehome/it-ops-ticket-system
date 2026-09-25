import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service: AxiosInstance = axios.create({
  baseURL: '/',
  timeout: 30000
})

service.interceptors.request.use((config) => {
  const token = localStorage.getItem('itops_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    // 文件流直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    const res = response.data
    if (res.code === 200) {
      return res
    }
    if (res.code === 401) {
      handleUnauthorized(res.msg || '登录已过期，请重新登录')
      return Promise.reject(new Error(res.msg))
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || 'Error'))
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      handleUnauthorized('登录已过期，请重新登录')
    } else if (status === 403) {
      ElMessage.error('没有操作权限')
    } else {
      const msg = error.response?.data?.msg || error.message || '网络异常'
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

let unauthorizedHandled = false
function handleUnauthorized(msg: string) {
  if (unauthorizedHandled) return
  unauthorizedHandled = true
  ElMessage.error(msg)
  localStorage.removeItem('itops_token')
  router.replace(`/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`)
  setTimeout(() => (unauthorizedHandled = false), 1500)
}

export interface ApiResult<T = any> {
  code: number
  msg: string
  data: T
}

export function get<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
  return service.get(url, { params, ...config }) as any
}
export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
  return service.post(url, data, config) as any
}
export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
  return service.put(url, data, config) as any
}
export function del<T = any>(url: string, params?: any): Promise<ApiResult<T>> {
  return service.delete(url, { params }) as any
}

/** 下载导出文件 */
export async function downloadFile(url: string, params: any, fallbackName: string) {
  const resp = await service.get(url, { params, responseType: 'blob' })
  const disposition = (resp.headers['content-disposition'] || '') as string
  let fileName = fallbackName
  const star = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  const plain = disposition.match(/filename="?([^";]+)"?/i)
  if (star) {
    fileName = decodeURIComponent(star[1])
  } else if (plain) {
    fileName = decodeURIComponent(plain[1])
  }
  const blob = new Blob([resp.data])
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = fileName
  link.click()
  URL.revokeObjectURL(link.href)
}

export default service
