FROM openjdk:21-jdk-slim
WORKDIR /app
ARG JAR_FILE=build/libs/product-service-*.jar
COPY ${JAR_FILE} /app/product-service.jar
ENTRYPOINT ["java", "-jar", "/app/product-service.jar"]