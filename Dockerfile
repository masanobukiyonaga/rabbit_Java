# Build Stage
FROM maven:3.9.6-amazoncorretto-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run Stage
FROM amazoncorretto:17
WORKDIR /app
COPY --from=build /app/target/rabbit-java-0.0.1-SNAPSHOT.jar app.jar
COPY .env .env

# Environment variables (Defaults, can be overridden)
ENV APP_USERNAME=admin
ENV APP_PASSWORD=8108za10
# DB_PASSWORD should be passed at runtime or via .env

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
