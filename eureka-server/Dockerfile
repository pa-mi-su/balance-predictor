# ---- build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Parent POM (needed by child’s <parent> section)
COPY pom.xml ./pom.xml

# Module POM (build only this module)
COPY eureka-server/pom.xml eureka-server/pom.xml

# Go offline just for this module (and parent)
RUN mvn -q -B -U -f eureka-server/pom.xml -DskipTests dependency:go-offline

# Copy module sources and package
COPY eureka-server/src eureka-server/src
RUN mvn -q -B -f eureka-server/pom.xml -DskipTests package

# ---- run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/eureka-server/target/eureka-server-*.jar /app/app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","/app/app.jar"]
