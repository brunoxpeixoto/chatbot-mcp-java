# Build: compila o jar dentro de um container com Maven + JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# Runtime: so o JRE + o jar, sem Maven/JDK/codigo-fonte
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/api-rest-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
