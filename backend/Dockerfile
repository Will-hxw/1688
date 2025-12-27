# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制pom.xml并下载依赖（利用Docker缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre

WORKDIR /app

# 创建非root用户
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# 创建上传目录
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

# 从构建阶段复制jar包
COPY --from=builder /app/target/*.jar app.jar

# 切换到非root用户
USER appuser

# 暴露端口
EXPOSE 8080

# JVM优化参数
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
