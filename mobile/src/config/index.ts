/**
 * 服务地址配置
 * - H5 走 vite 代理 /api
 * - App(真机/APK) 直连后端, 部署时改为实际服务器地址(支持 http/https、域名)
 */
let BASE_URL = '/api'
// #ifdef APP-PLUS
// 真机/APK 直连后端: 局域网用电脑 IP, 公网用域名; 端口 8080 为后端服务
BASE_URL = 'http://10.101.208.157:8080/api'
// #endif

export { BASE_URL }
