FROM openjdk:21-jdk-alpine
WORKDIR /app
ARG JAR_FILE=build/libs/discovery-server-*.jar
COPY ${JAR_FILE} /app/discovery-server.jar
ENTRYPOINT ["java", "-jar", "/app/discovery-server.jar"]