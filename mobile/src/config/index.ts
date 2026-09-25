/**
 * 服务地址配置
 * - H5 走 vite 代理 /api
 * - App(真机/APK) 直连后端, 部署时改为实际服务器地址(支持 http/https、域名)
 */
let BASE_URL = '/api'
// #ifdef APP-PLUS
BASE_URL = 'http://192.168.1.100:8080/api'
// #endif

export { BASE_URL }
