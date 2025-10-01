# -------- Build stage --------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Bring in the multi-module POM graph
COPY pom.xml ./pom.xml
COPY balance-service/pom.xml balance-service/pom.xml
COPY ledger-service/pom.xml  ledger-service/pom.xml
COPY plaid-service/pom.xml   plaid-service/pom.xml
COPY eureka-server/pom.xml   eureka-server/pom.xml

# Prefetch deps
RUN mvn -q -B -U -DskipTests dependency:go-offline

# Copy only this module's sources and build it
COPY eureka-server/src eureka-server/src
RUN mvn -q -B -DskipTests -pl eureka-server -am package

# -------- Runtime stage --------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/eureka-server/target/eureka-server-*.jar /app/app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","/app/app.jar"]
