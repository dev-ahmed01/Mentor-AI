FROM eclipse-temurin:25.0.4_7-jdk-alpine AS build
WORKDIR /build
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:25.0.4_7-jre-alpine
RUN addgroup -S mentorai && adduser -S -G mentorai mentorai
WORKDIR /app
COPY --from=build --chown=mentorai:mentorai /build/target/mentorai-backend-0.1.0-SNAPSHOT.jar app.jar
USER mentorai
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 CMD wget -q --spider http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-Duser.timezone=UTC", "-jar", "/app/app.jar"]
