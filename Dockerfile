# ==========================================
# ETAPA 1: Compilar la aplicación
# ==========================================

FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar el archivo de Maven primero
COPY pom.xml .

# Copiar el código fuente
COPY src ./src

# Compilar y generar el JAR
RUN mvn clean package -DskipTests


# ==========================================
# ETAPA 2: Ejecutar la aplicación
# ==========================================

FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el JAR generado en la etapa anterior
COPY --from=build /app/target/campusgo-0.0.1-SNAPSHOT.jar app.jar

# Documentar el puerto utilizado por CampusGo
EXPOSE 8081

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]