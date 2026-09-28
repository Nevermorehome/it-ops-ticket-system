/**
 * 服务地址配置
 * - H5 走 vite 代理 /api（用户一般无需修改）
 * - App(真机/APK) 直连后端, 首次启动进入"服务器配置"页, 用户输入地址后保存
 *   未配置时使用代码内置默认值（仅作兜底, 部署后应改为实际地址）
 */

/** App 端内置默认地址（仅当用户未配置时使用） */
const DEFAULT_APP_BASE_URL = 'http://10.101.208.157:8080/api'

/** H5 端固定走 vite 代理 */
const DEFAULT_H5_BASE_URL = '/api'

/** 存储用户配置地址的 key */
const STORAGE_KEY = 'itops_server_url'

/** 获取当前应使用的 BASE_URL：优先用户配置, 其次内置默认 */
export function getBaseUrl(): string {
  // #ifdef H5
  return DEFAULT_H5_BASE_URL
  // #endif
  // #ifdef APP-PLUS
  const saved = uni.getStorageSync(STORAGE_KEY)
  return saved || DEFAULT_APP_BASE_URL
  // #endif
  // #ifndef H5 || APP-PLUS
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
