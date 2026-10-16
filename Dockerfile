# Backend de Java Practice Lab (contexto de build = raiz del repositorio).
#
# El runner lanza contenedores desechables (javalab-runner:17, javalab-runner-python:3.12) para
# ejecutar el codigo del alumno. Para eso el backend necesita el CLI de docker (se copia abajo) y
# acceso al daemon del host (en el despliegue se monta /var/run/docker.sock). Ademas comparte un
# directorio con los runners (JAVALAB_RUNNER_WORK_DIR / JAVALAB_RUNNER_HOST_WORK_DIR).
#
# La variable JAVALAB_RUNNER_USE_DOCKER se puede sobreescribir en el entorno del despliegue.
# Por defecto queda en true (Docker); el modo local solo sirve para Java y no tiene Python.

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
# Etapa aparte para traer el CLI de docker: `COPY --from` no admite variables en el nombre de la imagen,
# asi que se resuelve con un FROM fijo y un alias.
FROM docker:27.3.1-cli AS dockercli

FROM eclipse-temurin:17-jdk
ARG JUNIT_VERSION=1.12.2

# El backend lanza los runners con el CLI de Docker, asi que el binario tiene que estar en la imagen.
# Se copia solo el cliente (no el daemon) desde la etapa de arriba; pesa unos pocos MB.
COPY --from=dockercli /usr/local/bin/docker /usr/local/bin/docker

# Usuario sin privilegios: el codigo del alumno se ejecuta con este usuario, nunca como root.
RUN useradd --system --uid 10001 --no-create-home --shell /usr/sbin/nologin app
WORKDIR /app

# Directorio compartido con los runners: el backend escribe aqui y los contenedores lo montan con la
# MISMA ruta. En el despliegue, Coolify monta un directorio del HOST en /work (con JAVALAB_RUNNER_WORK_DIR=/work
# y JAVALAB_RUNNER_HOST_WORK_DIR=<ruta en el host>). El propietario tiene que ser el UID de 'app' (10001)
# EN EL HOST: el bind-mount oculta lo que se cree aqui, por eso hay que preparar el directorio del host.
RUN mkdir -p /work && chown app:app /work

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
    JAVALAB_RUNNER_USE_DOCKER=true \
    JAVALAB_RUNNER_WORK_DIR=/work \
    # Heap moderado: el servidor atiende la API y ademas coordina los contenedores del runner.
    JAVA_OPTS="-Xmx512m -XX:MaxMetaspaceSize=256m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"

USER app
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
