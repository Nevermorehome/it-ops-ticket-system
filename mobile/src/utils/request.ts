import { BASE_URL } from '@/config'

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  showLoading?: boolean
}

export function request<T = any>(options: RequestOptions): Promise<T> {
  const token = uni.getStorageSync('itops_token')
  if (options.showLoading) {
    uni.showLoading({ title: '加载中', mask: true })
  }
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      success: (res) => {
        const body = res.data as any
        if (res.statusCode === 200 && body.code === 200) {
          resolve(body.data as T)
          return
        }
        if (body.code === 401 || res.statusCode === 401) {
          handleUnauthorized(body.msg || '登录已过期')
          reject(new Error(body.msg || '未登录'))
          return
        }
        const msg = body.msg || `请求失败(${res.statusCode})`
        uni.showToast({ title: msg, icon: 'none' })
        reject(new Error(msg))
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常，请检查网络或服务器地址', icon: 'none' })
        reject(err)
      },
      complete: () => {
        if (options.showLoading) uni.hideLoading()
      }
    })
  })
}

let locked = false
function handleUnauthorized(msg: string) {
  if (locked) return
  locked = true
  uni.showToast({ title: msg, icon: 'none' })
  uni.removeStorageSync('itops_token')
  setTimeout(() => {
    uni.reLaunch({ url: '/pages/login/index' })
    locked = false
  }, 800)
}

/** 上传单文件(图片) */
export function uploadFile(filePath: string): Promise<any> {
  const token = uni.getStorageSync('itops_token')
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE_URL + '/file/upload',
      filePath,
      name: 'file',
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => {
        try {
          const body = JSON.parse(res.data)
          if (body.code === 200) {
            resolve(body.data)
          } else {
            uni.showToast({ title: body.msg || '上传失败', icon: 'none' })
            reject(new Error(body.msg))
          }
        } catch {
          reject(new Error('上传响应解析失败'))
        }
      },
      fail: reject
    })
  })
}
