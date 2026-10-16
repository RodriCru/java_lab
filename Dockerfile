# Backend de Java Practice Lab (contexto de build = raiz del repositorio).
#
# El backend NO ejecuta codigo: le pasa el envio al runner-service por HTTP y muestra el resultado.
# Por eso ya no instala el CLI de docker, ni monta el socket, ni comparte directorios. Es una simple
# API. El runner-service (su propia imagen) es quien compila y ejecuta.

# --- 1) Compilar el jar (reactor: padre + core + backend) -------------------------------------------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /build

# Primero lo que cambia poco, para aprovechar la cache de capas.
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY runner-core/pom.xml runner-core/pom.xml
COPY runner-service/pom.xml runner-service/pom.xml
COPY backend/pom.xml backend/pom.xml
RUN chmod +x mvnw && ./mvnw -q -B -N install && ./mvnw -q -B -pl runner-core,backend -am dependency:go-offline

COPY runner-core/src runner-core/src
COPY backend/src backend/src
RUN ./mvnw -q -B -pl runner-core,backend -am -DskipTests package && cp backend/target/*.jar /build/app.jar

# --- 2) Imagen final -------------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk

RUN useradd --system --uid 10001 --no-create-home --shell /usr/sbin/nologin app
WORKDIR /app

COPY --from=build /build/app.jar app.jar
COPY exercises exercises
COPY questions questions

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVALAB_EXERCISES_DIR=/app/exercises \
    JAVALAB_QUESTIONS_DIR=/app/questions \
    JAVA_OPTS="-Xmx384m -XX:MaxMetaspaceSize=192m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"

USER app
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
