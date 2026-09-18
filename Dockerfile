# 构建阶段
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# 是否启用AI摘要模块（默认关闭；启用时以 -Pwith-ai 构建，将 langchain4j 与 AI 模块打进产物）
ARG AI_ENABLED=false

# 复制全部模块的pom文件并提前下载依赖（go-offline 需解析完整 reactor，子模块 pom 缺一不可；
# AI_ENABLED=true 时同时预下载 langchain4j 依赖，加快后续构建）
COPY Backend/pom.xml .
COPY Backend/WuTao-common/pom.xml WuTao-common/pom.xml
COPY Backend/WuTao-pojo/pom.xml WuTao-pojo/pom.xml
COPY Backend/WuTao-extension-api/pom.xml WuTao-extension-api/pom.xml
COPY Backend/WuTao-server/pom.xml WuTao-server/pom.xml
COPY Backend/WuTao-ai/pom.xml WuTao-ai/pom.xml
RUN if [ "$AI_ENABLED" = "true" ]; then \
      mvn dependency:go-offline -B -Pwith-ai; \
    else \
      mvn dependency:go-offline -B; \
    fi

# 复制源代码并构建
COPY Backend/ .
RUN if [ "$AI_ENABLED" = "true" ]; then \
      mvn clean package -DskipTests -B -Pwith-ai; \
    else \
      mvn clean package -DskipTests -B; \
    fi

# 运行阶段
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="wutao"

# 创建非root用户
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -s /bin/sh -D appuser

WORKDIR /app

# 复制构建好的JAR包
COPY --from=builder /build/WuTao-server/target/WuTao-server-1.0-SNAPSHOT.jar ./wutao.jar

# 创建日志目录
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

# 切换到非root用户
USER appuser

# JVM参数（2G 内存服务器优化版）
# 堆内存 384m + 元空间 192m + 直接内存 128m ≈ 700m，
# 留出足够内存给 MySQL/Redis/Nginx/系统
ENV JAVA_OPTS="-XX:+UseG1GC -Xms128m -Xmx384m -XX:MaxMetaspaceSize=192m -XX:MaxDirectMemorySize=128m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs -XX:+ExitOnOutOfMemoryError"

# 暴露端口
EXPOSE 5922

# 启动命令
# spring.profiles.active 由 docker-compose 环境变量 SPRING_PROFILES_ACTIVE 传入
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar wutao.jar"]
