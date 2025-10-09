# ---- build stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY ledger-service/pom.xml ledger-service/pom.xml

RUN mvn -q -B -U -f ledger-service/pom.xml -DskipTests dependency:go-offline

COPY ledger-service/src ledger-service/src
RUN mvn -q -B -f ledger-service/pom.xml -DskipTests package

# ---- run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/ledger-service/target/ledger-service-*.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
