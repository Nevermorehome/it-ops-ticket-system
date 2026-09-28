/**
 * 服务地址配置
 * - 默认值: H5 走 vite 代理 /api; App/小程序 直连 https://itops.jsfqal.cn/api
 * - 用户可在"服务器配置"页输入地址保存, 保存后所有端均优先使用用户配置
 *   (H5 端配置绝对地址会触发跨域, 需后端 CORS 放行; dev 模式后端 CORS=*)
 */

/** App 端内置默认地址（仅当用户未配置时使用） */
const DEFAULT_APP_BASE_URL = 'https://itops.jsfqal.cn/api'

/** H5 端固定走 vite 代理 */
const DEFAULT_H5_BASE_URL = '/api'

/** 微信小程序端默认地址（必须 HTTPS 公网域名, 且在小程序后台配置为 request 合法域名） */
const DEFAULT_MP_BASE_URL = 'https://itops.jsfqal.cn/api'

/** 存储用户配置地址的 key */
const STORAGE_KEY = 'itops_server_url'

/** 获取当前应使用的 BASE_URL：优先用户配置, 其次内置默认 */
export function getBaseUrl(): string {
  const saved = uni.getStorageSync(STORAGE_KEY)
  if (saved) return saved
  // #ifdef H5
  return DEFAULT_H5_BASE_URL
  // #endif
  // #ifdef APP-PLUS
  return DEFAULT_APP_BASE_URL
  // #endif
  // #ifdef MP
  return DEFAULT_MP_BASE_URL
  // #endif
  // #ifndef H5 || APP-PLUS || MP
  return DEFAULT_H5_BASE_URL
  // #endif
}

/** 保存用户配置的服务器地址 */
export function setBaseUrl(url: string): void {
  uni.setStorageSync(STORAGE_KEY, url)
}

/** 清除用户配置, 回退到内置默认值 */
export function clearBaseUrl(): void {
  uni.removeStorageSync(STORAGE_KEY)
}

/** 是否已配置过服务器地址（首次安装判断用） */
export function hasServerUrl(): boolean {
  return !!uni.getStorageSync(STORAGE_KEY)
}

/** 获取内置默认 App 地址（配置页"恢复默认"用） */
export function getDefaultAppUrl(): string {
  return DEFAULT_APP_BASE_URL
}
