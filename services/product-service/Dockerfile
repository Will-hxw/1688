# Product Service Dockerfile
# 多阶段构建：构建阶段 + 运行阶段

# ==================== 构建阶段 ====================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 复制父 POM
COPY pom.xml ./pom.xml

# 安装父 POM 到本地仓库（解决依赖解析问题）
RUN mvn -N install -DskipTests -B -q

# 复制 common 模块
COPY common/pom.xml ./common/pom.xml
COPY common/src ./common/src

# 构建并安装 common 模块
RUN mvn -f common/pom.xml clean install -DskipTests -B -q

# 复制当前服务
COPY product-service/pom.xml ./product-service/pom.xml
COPY product-service/src ./product-service/src

# 构建 product-service
RUN mvn -f product-service/pom.xml clean package -DskipTests -B -q

# ==================== 运行阶段 ====================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建非 root 用户和上传目录
RUN addgroup -S appgroup && adduser -S appuser -G appgroup \
    && mkdir -p /app/uploads \
    && chown -R appuser:appgroup /app

# 从构建阶段复制 jar 包
COPY --from=builder /build/product-service/target/*.jar app.jar

# 设置文件权限
RUN chown appuser:appgroup app.jar

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8082

# JVM 优化参数
ENV JAVA_OPTS="-Xms128m -Xmx256m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
