FROM openjdk:23-slim
WORKDIR /app
COPY target/app.jar app.jar
COPY wallet /app/wallet
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080