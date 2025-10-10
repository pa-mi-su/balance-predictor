# ---- build stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy the root and module POMs to leverage dependency layer caching
COPY pom.xml ./pom.xml
COPY auth-service/pom.xml auth-service/pom.xml

# Pre-fetch dependencies for faster subsequent builds
RUN mvn -q -B -U -f auth-service/pom.xml -DskipTests dependency:go-offline

# Copy source and build the Spring Boot fat jar
COPY auth-service/src auth-service/src
RUN mvn -q -B -f auth-service/pom.xml -DskipTests package

# ---- run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /workspace/auth-service/target/auth-service-*.jar /app/app.jar

# Expose the service port
EXPOSE 8084

# Run!
ENTRYPOINT ["java","-jar","/app/app.jar"]
