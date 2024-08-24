FROM openjdk:21-jdk-alpine
WORKDIR /app
ARG JAR_FILE=build/libs/api-gateway-*.jar
COPY ${JAR_FILE} /app/api-gateway.jar
ENTRYPOINT ["java", "-jar", "/app/api-gateway.jar"]