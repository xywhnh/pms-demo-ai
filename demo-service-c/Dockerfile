ARG MVN_IMAGE=maven:3.9.9-eclipse-temurin-21
ARG JRE_IMAGE=eclipse-temurin:21-jre

FROM ${MVN_IMAGE} AS builder

WORKDIR /workspace
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH=${JAVA_HOME}/bin:${PATH}

COPY pom.xml .
COPY demo-service-a/pom.xml demo-service-a/pom.xml
COPY demo-service-b/pom.xml demo-service-b/pom.xml
COPY demo-service-c/pom.xml demo-service-c/pom.xml
COPY demo-service-c/src demo-service-c/src

RUN java -version && mvn -B -DskipTests -f demo-service-c/pom.xml clean package

FROM ${JRE_IMAGE}

WORKDIR /app
RUN mkdir -p /app/logs

COPY --from=builder /workspace/demo-service-c/target/demo-service-c-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
