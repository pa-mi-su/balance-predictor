# ---- build stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY plaid-service/pom.xml plaid-service/pom.xml

RUN mvn -q -B -U -f plaid-service/pom.xml -DskipTests dependency:go-offline

COPY plaid-service/src plaid-service/src
RUN mvn -q -B -f plaid-service/pom.xml -DskipTests package

# ---- run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/plaid-service/target/plaid-service-*.jar /app/app.jar
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
