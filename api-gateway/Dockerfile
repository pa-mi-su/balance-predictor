# ---- build stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml

RUN mvn -q -B -U -f api-gateway/pom.xml -DskipTests dependency:go-offline

COPY api-gateway/src api-gateway/src
RUN mvn -q -B -f api-gateway/pom.xml -DskipTests package

# ---- run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/api-gateway-*.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
