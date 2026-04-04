FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

COPY pom.xml .
COPY demo-service-a/pom.xml demo-service-a/pom.xml
COPY demo-service-a/src demo-service-a/src

RUN mvn -B -DskipTests -pl demo-service-a -am clean package

FROM eclipse-temurin:21-jre

WORKDIR /app
RUN mkdir -p /app/logs

COPY --from=builder /workspace/demo-service-a/target/demo-service-a-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
