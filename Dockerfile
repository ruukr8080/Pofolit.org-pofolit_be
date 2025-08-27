FROM gradle:8.13-jdk17 AS build

WORKDIR /app-api

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

COPY src ./src
RUN gradle build -x test --no-daemon

FROM openjdk:17-slim
WORKDIR /app-api


ENV DB_URL="jdbc:mysql://localhost:3306/pofolitdb?serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
ENV DB_USERNAME="mysql"
ENV DB_PASSWORD="sa"
ENV REDIS_HOST="127.0.0.1"
ENV REDIS_PORT="6379"
ENV JWT_SECRET="popopopopopfolitsercverpopopopopopfolitsercverpopopopopopfolitsercverpopopopopopfolitsercverpopopopopopfolitsercverpopopopopopfolitsercverpopopopopopfolitsercverpopopopopopfolitsercverpopopopopopfolitsercver"
ENV JWT_EXP="36000"
ENV GOOGLE_CLIENT_ID=""
ENV GOOGLE_CLIENT_SECRET=""
ENV KAKAO_CLIENT_ID=""
ENV KAKAO_CLIENT_SECRET=""

COPY --from=build /app-api/build/libs/*.jar ./app-api.jar

ENTRYPOINT ["java", "-jar", "app-api.jar"]