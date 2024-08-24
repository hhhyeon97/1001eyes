FROM openjdk:21-jdk-alpine
WORKDIR /app
ARG JAR_FILE=build/libs/user-service-*.jar
COPY ${JAR_FILE} /app/user-service.jar
ENTRYPOINT ["java", "-jar", "/app/user-service.jar"]