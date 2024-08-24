FROM openjdk:21-jdk-alpine
WORKDIR /app
ARG JAR_FILE=build/libs/order-service-*.jar
COPY ${JAR_FILE} /app/order-service.jar
ENTRYPOINT ["java", "-jar", "/app/order-service.jar"]