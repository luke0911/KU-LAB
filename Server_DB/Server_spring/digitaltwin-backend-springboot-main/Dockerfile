FROM gradle:8.14.0-jdk17-alpine AS build

WORKDIR /app
COPY gradlew /app/gradlew
COPY gradle /app/gradle
COPY build.gradle /app/build.gradle
COPY settings.gradle /app/settings.gradle
COPY src /app/src

RUN ./gradlew clean build


FROM eclipse-temurin:17-jre-alpine
ARG FILE_DIRECTORY
COPY --from=build /app/build/libs/*SNAPSHOT.jar /app/dt_neb_spring.jar

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "/app/dt_neb_spring.jar"]