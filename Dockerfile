# Backend de Java Practice Lab para Render (contexto de build = raiz del repositorio).
#
# El runner usa el modo local (JAVALAB_RUNNER_USE_DOCKER=false): un contenedor no puede lanzar otros
# contenedores, asi que el codigo del alumno se compila y ejecuta aqui dentro. Por eso la imagen final
# es un JDK (hace falta javac) y no un JRE.

# --- 1) Compilar el jar ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /build/backend

# Primero lo que cambia poco, para aprovechar la cache de capas de Docker.
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline

COPY backend/src src
RUN ./mvnw -q -B -DskipTests package && cp target/*.jar /build/app.jar

# --- 2) Imagen final -------------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk
ARG JUNIT_VERSION=1.12.2

# Usuario sin privilegios: el codigo del alumno se ejecuta con este usuario, nunca como root.
RUN useradd --system --uid 10001 --no-create-home --shell /usr/sbin/nologin app
WORKDIR /app

COPY --from=build /build/app.jar app.jar
COPY exercises exercises
COPY questions questions

# El jar de JUnit no esta en el repositorio: se descarga igual que en runner/build.sh.
ADD https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/${JUNIT_VERSION}/junit-platform-console-standalone-${JUNIT_VERSION}.jar /app/runner/junit-platform-console-standalone.jar
RUN chmod 644 /app/runner/junit-platform-console-standalone.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVALAB_EXERCISES_DIR=/app/exercises \
    JAVALAB_QUESTIONS_DIR=/app/questions \
    JAVALAB_RUNNER_JUNIT_JAR=/app/runner/junit-platform-console-standalone.jar \
    JAVALAB_RUNNER_USE_DOCKER=false \
    # Render gratis tiene 512 MB: heap pequeno para dejar sitio a javac y a la JVM del alumno.
    JAVA_OPTS="-Xmx192m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"

USER app
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
