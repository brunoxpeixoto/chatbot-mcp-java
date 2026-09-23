FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
# "chatbot-*.jar" casa com o jar "sombreado" (com dependencias), gerado pelo
# maven-shade-plugin. Nao casa com "original-chatbot-*.jar" (o jar sem as
# dependencias, que o shade-plugin tambem deixa no target/).
COPY --from=build /app/target/chatbot-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
