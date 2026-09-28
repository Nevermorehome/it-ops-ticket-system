# syntax=docker/dockerfile:1.7
# ============================================================
# 多阶段构建: maven 打包 + JRE 21 运行
#   docker build -t itops-backend:1.0.0 ./backend
#   或经 docker compose build 自动构建
# ============================================================

# ---------- Stage 1: 构建 ----------
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# 先拷 pom, 利用 BuildKit cache mount 预下依赖(加速增量构建)
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

# 再拷源码打包
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -B \
    && mv target/itops-backend.jar /build/app.jar

# ---------- Stage 2: 运行 ----------
FROM eclipse-temurin:21-jre AS runtime
LABEL maintainer="itops-team"
LABEL description="信息部内部 IT 运维工单记录系统 - 后端"

WORKDIR /app

# 时区与字符集
ENV TZ=Asia/Shanghai \
    LANG=C.UTF-8 \
    JAVA_OPTS="-Xms256m -Xmx768m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs"

COPY --from=builder /build/app.jar /app/app.jar

EXPOSE 8080

# 健康检查(Actuator 已匿名开放 /actuator/health)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
