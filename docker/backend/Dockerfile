# Étape 1 : build du JAR avec Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : image légère avec Amazon Corretto
FROM amazoncorretto:17

ARG PROFILE=dev
ARG APP_VERSION=1.0.0

WORKDIR /app

# Copy and rename the JAR to a fixed name
COPY --from=build /build/target/ITSM-*.jar /app/app.jar

# Configuration de l'environnement
ENV SPRING_PROFILES_ACTIVE=${PROFILE}
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-sql-itsm:5432/itsm_thales
ENV SPRING_REDIS_HOST=redis
ENV SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
ENV EMAIL_HOSTNAME=missing_host_name
ENV EMAIL_USER_NAME=missing_user_name
ENV EMAIL_PASSWORD=missing_password

EXPOSE 8090

CMD java -jar \
  -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} \
  -Dspring.datasource.url=${SPRING_DATASOURCE_URL} \
  -Dspring.data.redis.host=${SPRING_REDIS_HOST} \
  -Dspring.kafka.bootstrap-servers=${SPRING_KAFKA_BOOTSTRAP_SERVERS} \
  /app/app.jar
