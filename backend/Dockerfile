FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /build

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    GRADLE_OPTS="-Xmx512m -Xms256m" \
    ./gradlew dependencies --no-daemon

COPY src ./src
RUN --mount=type=cache,target=/root/.gradle \
    GRADLE_OPTS="-Xmx512m -Xms256m" \
    ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /build/build/libs/news-portal-api.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8084

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
