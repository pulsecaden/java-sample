FROM amazoncorretto:21-al2023

WORKDIR /app
COPY target/springboot-microservice-0.1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
