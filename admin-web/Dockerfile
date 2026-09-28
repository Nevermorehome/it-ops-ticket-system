# ============================================================
# admin-web 多阶段构建: node 构建 dist + nginx 托管静态文件
#   docker build -t itops-admin-web:1.0.0 ./admin-web
# 注意: 依赖 BuildKit(默认启用), 用 --mount=type=cache 加速 npm install
# ============================================================

# ---------- Stage 1: 构建 ----------
FROM node:20-alpine AS builder
WORKDIR /build

# 配置淘宝 npm 镜像加速(国内构建从 npmjs 下载极慢)
RUN npm config set registry https://registry.npmmirror.com

# 先拷 package 清单, 利用 BuildKit cache mount 加速 npm install
COPY package*.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --no-audit --no-fund

# 再拷源码构建
COPY . .
RUN npm run build

# ---------- Stage 2: 运行 ----------
FROM nginx:1.27-alpine AS runtime
LABEL maintainer="itops-team"
LABEL description="信息部 IT 运维工单系统 - 管理后台(admin-web)"

# 拷自定义 nginx 配置(反代 /api /uploads 到 backend 容器)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 拷构建产物到 nginx 静态目录
COPY --from=builder /build/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -q --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
