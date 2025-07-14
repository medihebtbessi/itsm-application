FROM openjdk:17-alpine
ARG ITSM=target/*.jar
COPY ./target/ITSM.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]