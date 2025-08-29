FROM gradle:8.13-jdk17 AS build

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

COPY src ./src
RUN gradle build -x test --no-daemon

FROM openjdk:17-slim
WORKDIR /app

COPY --from=build /app/build/libs/*.jar ./app.jar

ENTRYPOINT ["java", "-jar", "./app.jar", "--spring.profiles.active=dev"]