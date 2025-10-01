FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY balance-service/pom.xml balance-service/pom.xml
COPY ledger-service/pom.xml  ledger-service/pom.xml
COPY plaid-service/pom.xml   plaid-service/pom.xml
COPY eureka-server/pom.xml   eureka-server/pom.xml

RUN mvn -q -B -U -DskipTests dependency:go-offline

COPY balance-service/src balance-service/src
RUN mvn -q -B -DskipTests -pl balance-service -am package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/balance-service/target/balance-service-*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
