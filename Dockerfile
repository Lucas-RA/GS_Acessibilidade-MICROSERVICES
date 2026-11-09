FROM maven:3.9.8-eclipse-temurin-17-alpine AS build
WORKDIR /opt/app
COPY . .
# Os testes rodam no pipeline de CI antes da imagem ser gerada
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-alpine-3.23
WORKDIR /opt/app
COPY --from=build /opt/app/target/app.jar /opt/app/app.jar

ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]
