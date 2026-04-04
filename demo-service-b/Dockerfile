FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

COPY pom.xml .
COPY demo-service-b/pom.xml demo-service-b/pom.xml
COPY demo-service-b/src demo-service-b/src

RUN mvn -B -DskipTests -pl demo-service-b -am clean package

FROM eclipse-temurin:21-jre

WORKDIR /app
RUN mkdir -p /app/logs

COPY --from=builder /workspace/demo-service-b/target/demo-service-b-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
