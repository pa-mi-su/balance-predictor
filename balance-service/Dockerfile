# ---- build stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY balance-service/pom.xml balance-service/pom.xml

RUN mvn -q -B -U -f balance-service/pom.xml -DskipTests dependency:go-offline

COPY balance-service/src balance-service/src
RUN mvn -q -B -f balance-service/pom.xml -DskipTests package

# ---- run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/balance-service/target/balance-service-*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
