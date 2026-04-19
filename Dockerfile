FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/app.jar crb-processing-0.0.1.jar
EXPOSE 5900
ENTRYPOINT ["java", "-jar", "crb-processing-0.0.1.jar"]