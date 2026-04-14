<<<<<<< HEAD
# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制 pom.xml 和源代码
COPY pom.xml .
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre

WORKDIR /app

# 复制构建好的 jar 文件
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
=======
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

RUN ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/istudyspot-backend-1.0.0.jar"]
>>>>>>> d1d4e326aa89a66aefc2f368a5a726a7d2d63cf7
