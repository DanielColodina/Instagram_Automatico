FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/instagram-auto-post-1.0.0.jar /app/app.jar
RUN mkdir -p /app/config /app/data/posts
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
