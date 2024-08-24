FROM openjdk:21-jdk-alpine
WORKDIR /app
ARG JAR_FILE=build/libs/product-service-*.jar
COPY ${JAR_FILE} /app/product-service.jar
ENTRYPOINT ["java", "-jar", "/app/product-service.jar"]