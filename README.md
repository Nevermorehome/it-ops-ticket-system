# 信息部内部 IT 运维工单记录系统

> 报修 → 派单 → 现场处理（GPS + 拍照水印）→ 解决关闭 → 统计分析 全流程闭环
> Monorepo：Spring Boot 3 后端 · Vue3 管理后台 · UniApp 移动端(H5/Android APK) · Nginx 本地部署

## 目录结构

```
.
├── backend/      # Spring Boot 3 + Spring Security + JWT + MyBatis-Plus + MySQL 8
├── admin-web/    # Vue3 + Vite + TS + Element Plus + ECharts 管理后台
├── mobile/       # UniApp + Vue3 + TS + Pinia 移动端(可打包 Android APK)
├── deploy/       # Nginx 配置、MySQL 初始化脚本
└── .trae/specs/  # 需求规格与实施计划
```

## 功能概览

- **认证与权限**：JWT 登录、用户/部门/角色/菜单管理，5 级数据权限（全部 / 自定义部门 / 本部门 / 本部门及以下 / 仅本人）
- **工单全生命周期**：创建、编辑、指派、转派、协同、受理、处理、挂起、恢复、解决、关闭（满意度评价）、重开、取消、合并，全程时间线留痕
- **现场记录（移动端）**：GPS 定位、地图选点、拍照/相册多图、自动压缩、四要素水印（时间 / 地址 / 经纬度 / 操作人）
- **报表中心**：日报/周报/月报/自定义区间，趋势、分布、处理人排行、部门统计，支持导出 Excel / Word / PDF
- **消息通知**：站内信（铃铛 + 未读角标）、企业微信 / 钉钉 / 飞书 Webhook（可配置触发事件、钉钉加签）
- **系统管理**：字典、参数配置、常用地点、资产、登录日志、操作日志

## 默认账号

| 账号 | 密码 | 角色 |
|---|---|---|
| admin | admin123 | 超级管理员 |
| wangwu | admin123 | 调度主管（本部门及以下数据） |
| lisi / zhaoliu | admin123 | 运维工程师（仅本人相关） |
| zhangsan / qianqi | admin123 | 报修用户（仅本人提交） |

---

## 一、本地开发

### 环境要求

- JDK 17+（已在 JDK 21 上验证构建与运行）、Maven 3.9+
- Node.js 18+（推荐 20）
- MySQL 8.0

### 1. 初始化数据库

```bash
mysql --default-character-set=utf8mb4 -uroot -p123456 -e "source deploy/mysql/init.sql"
```

默认连接 `localhost:3306`，账号 `root/123456`，库名 `itops`，可在
`backend/src/main/resources/application-dev.yml` 中修改（也可用环境变量
`ITOPS_DB_HOST / ITOPS_DB_PORT / ITOPS_DB_USER / ITOPS_DB_PASSWORD` 覆盖）。

### 2. 启动后端（端口 8080）

```bash
cd backend
mvn spring-boot:run
# 或打包后运行:
# mvn clean package -DskipTests && java -jar target/itops-backend.jar
# 默认 profile=dev
```

接口文档：启动后访问 `http://localhost:8080/doc.html`（Knife4j）。

### 3. 启动管理后台（端口 5173）

```bash
cd admin-web
npm install
# 若本机 npm 拦截了 esbuild 的 postinstall 脚本：
# node node_modules/esbuild/install.js
npm run dev
```

访问 `http://localhost:5173`，开发服务器已代理 `/api`、`/uploads` 到 8080。

### 4. 启动移动端 H5（端口 5174）

```bash
cd mobile
npm install
npm run dev:h5
```

访问 `http://localhost:5174/mobile/`（H5 路由 base 为 `/mobile/`）。

构建 H5 产物：

```bash
npm run build:h5    # 产物在 dist/build/h5
```

> **App 端服务器地址**：App 真机无法使用 localhost，需修改
> [mobile/src/config/index.ts](mobile/src/config/index.ts) 中 `APP-PLUS` 条件编译分支的
> `BASE_URL`（占位为 `http://192.168.1.100:8080/api`），改为电脑局域网 IP 或部署后的正式域名。

### 5. 打包 Android APK

1. 打开 [HBuilderX](https://www.dcloud.io/hbuilderx.html)，「文件 → 导入 → 从本地目录导入」选择 `mobile/` 目录；
2. 打开 `src/manifest.json`：
   - 在「基础配置」中点击 **重新获取 DCloud AppID** 并保存；
   - 「App 模块配置」已勾选 Geolocation / Camera / Gallery；
   - 「App SDK 配置 → 地图」填入腾讯地图/高德/百度地图 Key（地图选点功能必需），Key 需在对应开放平台申请并绑定包名与 SHA1；
3. 菜单「发行 → 原生 App-云打包」，选择 Android，使用公共测试证书或自有证书，等待云端打包后下载 APK；
4. 离线打包/CLI 方式参考 UniApp 官方文档：<https://uniapp.dcloud.net.cn/tutorial/app-base-cli.html>。

> **消息推送**：如需 APP 离线推送，在 manifest.json「App 模块配置 → Push」中开启
> UniPush（个推）2.0，并在 DCloud 开发者中心完成厂商通道配置；当前业务通知已通过
> 站内信 + Webhook 送达，推送通道为可选增强。

### 6. 地图 Key 说明

| 平台 | 用途 | 配置位置 |
|---|---|---|
| 腾讯地图 | App `uni.chooseLocation` 地图选点 | `src/manifest.json` → sdkConfigs.maps.qqmap.key |
| 高德地图 | 备选地图 SDK | sdkConfigs.maps.amap.appkey |
| 百度地图 | 备选地图 SDK | sdkConfigs.maps.baidu.appkey |

H5 端地图选点依赖浏览器环境与地图厂商 H5 Key；GPS 定位（`uni.getLocation`）在
H5 下需 HTTPS 或 localhost 环境授权。

---

## 二、本地生产部署（不使用 Docker）

> 已在 Windows + JDK 21 + MySQL 8.0 实测：构建、prod 启动、健康检查、登录限流、
> 导出（Excel/Word/PDF）全流程通过。

生产环境必须使用 **prod profile** 启动，该 profile 下：

- 数据库密码、JWT 密钥**没有内置默认值**，缺失即拒绝启动（fail-fast）；
- 禁止使用仓库内置的开发 JWT 密钥（启动校验，防止密钥随公开仓库泄露）；
- 接口文档（Knife4j/Swagger）端点直接返回 401，不暴露任何接口结构；
- CORS 默认不输出跨域头（前后端同域，经 Nginx 反代）；
- 异常响应不含堆栈/SQL/内部信息；日志写入滚动文件；
- 登录失败 5 次锁定 10 分钟（按 IP + 账号），阈值可配；
- 开启 gzip、Hikari 连接池调优、`/actuator/health` 健康检查。

### 1. 准备数据库

```bash
mysql --default-character-set=utf8mb4 -uroot -p -e "source deploy/mysql/init.sql"
```

### 2. 构建并以 prod profile 启动后端（8080）

```bash
cd backend
mvn clean package -DskipTests

# Windows PowerShell
$env:ITOPS_DB_HOST="127.0.0.1"
$env:ITOPS_DB_USER="root"
$env:ITOPS_DB_PASSWORD="你的强密码"
$env:ITOPS_JWT_SECRET="至少64字节的随机字符串-建议用密码生成器生成并妥善保管-0123456789abcdef"
$env:ITOPS_UPLOAD_PATH="D:/itops/uploads"
java -jar target/itops-backend.jar --spring.profiles.active=prod
```

生成 JWT 密钥（任选）：

```powershell
# PowerShell：生成 64 字节(512bit)Base64 随机串
$b = New-Object byte[] 64
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b)
[Convert]::ToBase64String($b)
```

健康检查（无需认证）：`GET http://localhost:8080/actuator/health` → `{"status":"UP"}`

> 上线后请**立即在管理后台修改 admin 默认密码**（admin/admin123 仅为种子账号）。

### 3. 生产环境变量清单

| 变量 | 必需 | 说明 |
|---|---|---|
| ITOPS_DB_HOST | ✅ | 数据库地址（prod 无默认值） |
| ITOPS_DB_PORT / ITOPS_DB_NAME | 3306 / itops | 端口与库名 |
| ITOPS_DB_USER / ITOPS_DB_PASSWORD | ✅ | 数据库账号密码（prod 无默认值） |
| ITOPS_DB_POOL_MAX | 20 | Hikari 最大连接数 |
| ITOPS_DB_USE_SSL | false | 数据库连接是否走 SSL（跨机/公网链路设 true） |
| ITOPS_JWT_SECRET | ✅ | ≥64 字节随机串，禁止使用开发默认值 |
| ITOPS_JWT_EXPIRE_MINUTES | 720 | 令牌有效期（分钟） |
| ITOPS_UPLOAD_PATH | /data/uploads | 上传文件存储目录（需可写、定期备份） |
| ITOPS_CORS_ORIGINS | 空 | 前端独立域名部署时配置，如 `https://ops.example.com`，逗号分隔 |
| ITOPS_WEB_URL / ITOPS_H5_URL | 空 | Webhook 消息内的工单跳转链接 |
| ITOPS_FONT_PATH | Noto Sans CJK 路径 | PDF 导出中文字体，Windows 可设 `C:/Windows/Fonts/simsun.ttc` |
| ITOPS_LOGIN_MAX_FAIL / ITOPS_LOGIN_LOCK_MINUTES | 5 / 10 | 登录防爆破阈值与锁定时长 |
| LOG_PATH | ./logs | 日志输出目录 |

### 4. 构建前端静态产物

```bash
cd admin-web && npm install && npm run build   # 产物 admin-web/dist
cd mobile && npm install && npm run build:h5   # 产物 mobile/dist/build/h5
```

### 5. 使用本地 Nginx 托管与反向代理（推荐）

1. 安装 Nginx（Windows 下载解压版即可）；
2. 将 [deploy/nginx/nginx.conf](deploy/nginx/nginx.conf) 放入 Nginx 的 `conf.d/`
   （或并入 `conf/nginx.conf` 的 http 块），并按实际路径修改其中两处：
   - `root D:/worklog/admin-web/dist;`（管理后台产物目录）
   - `alias D:/worklog/mobile/dist/build/h5/;`（移动端 H5 产物目录）
3. 建议为 Nginx 配置 HTTPS（证书可用 mkcert 内网测试或正式证书），443 对外、8080 仅监听本机；
4. 启动后访问：

| 入口 | 地址 |
|---|---|
| Web 管理后台 | `https://你的域名/` |
| 移动端 H5 | `https://你的域名/mobile/` |
| 后端 API（经 Nginx 转发） | `https://你的域名/api/` |

Nginx 配置已包含：`/api`、`/uploads` 反代到 `127.0.0.1:8080`、SPA history 路由
fallback、gzip、`client_max_body_size 20m`。

### 6. 日志与运维

- prod 日志：`logs/itops-backend.log`（按天 + 100MB 滚动，保留 30 天）与
  `logs/itops-backend-error.log`（ERROR 单独归档，保留 90 天）；
- 建议用 [WinSW](https://github.com/winsw/winsw) 或 NSSM 将 jar 注册为 Windows 服务开机自启；
- 上传文件（`ITOPS_UPLOAD_PATH`）与数据库需纳入每日备份计划。

### 数据备份与恢复

```bash
# 备份数据库
mysqldump -uroot -p123456 --single-transaction itops > itops_backup.sql

# 恢复数据库
mysql --default-character-set=utf8mb4 -uroot -p123456 itops < itops_backup.sql

# 备份上传文件(默认在 backend/data/uploads)
xcopy /E /I backend\data\uploads backup\uploads
```

---

## 三、工单状态流转

```
PENDING 待受理 ──指派──▶ ASSIGNED 已指派 ──受理──▶ PROCESSING 处理中
                              │                      │  ├──挂起──▶ SUSPENDED 已挂起 ──恢复──┐
                              │                      │  ├──解决──▶ RESOLVED 已解决            │
                              │                      │  └──────────(转派/协同/合并)◀───────────┘
                              ▼                      ▼
                          RESOLVED ──关闭──▶ CLOSED 已关闭 ──重开──▶ PROCESSING
PENDING/ASSIGNED ──取消──▶ CANCELLED 已取消
```

操作按钮按当前状态与权限标识双重控制（前端控制显隐、后端强校验）。

## 四、Webhook 通知配置

在管理后台「消息通知 → Webhook 配置」中新增：

- **企业微信**：填写机器人 URL，事件选 `*` 或具体动作（如 ASSIGN、RESOLVE），消息为 Markdown；
- **钉钉**：可选填加签密钥（HmacSHA256，系统自动计算 timestamp/sign）；
- **飞书**：填写自定义机器人 URL，消息为文本格式。

通知由工单事件异步派发（事务提交后触发），第三方失败不影响工单主流程，仅记录日志。
