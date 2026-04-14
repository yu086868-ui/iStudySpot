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