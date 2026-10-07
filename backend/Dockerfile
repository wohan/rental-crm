FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY vendor/text-agent /opt/text-agent
RUN mvn -q -f /opt/text-agent/pom.xml install
COPY pom.xml .
COPY src ./src
RUN mvn -q -Ptext-agent -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends wget && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/rentcrm-backend-0.1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
