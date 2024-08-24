FROM openjdk:21-jdk-slim
WORKDIR /app
ARG JAR_FILE=build/libs/discovery-server-*.jar
COPY ${JAR_FILE} /app/discovery-server.jar
ENTRYPOINT ["java", "-jar", "/app/discovery-server.jar"]