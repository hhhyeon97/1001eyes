FROM openjdk:21-jdk-slim
WORKDIR /app
ARG JAR_FILE=build/libs/user-service-*.jar
COPY ${JAR_FILE} /app/user-service.jar
ENTRYPOINT ["java", "-jar", "/app/user-service.jar"]