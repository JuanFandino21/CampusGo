# ==========================================
# ETAPA 1: Compilar el microservicio
# ==========================================

FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn clean package -DskipTests


# ==========================================
# ETAPA 2: Ejecutar el microservicio
# ==========================================

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/campusgo-objetos-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
