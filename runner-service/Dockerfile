# Runner service: compila y ejecuta el codigo del alumno (Java, Python y los que vengan).
#
# Aqui viven los compiladores. Añadir un lenguaje = instalar su toolchain en esta imagen y sumar
# su entrada en RunnerEngine. No necesita Docker, ni sock, ni acceso al host.
FROM eclipse-temurin:17-jdk AS build
WORKDIR /build

# 1) Nucleo compartido + servicio
COPY runner-core/pom.xml runner-core/pom.xml
COPY runner-core/src runner-core/src
COPY runner-service/pom.xml runner-service/pom.xml
COPY runner-service/src runner-service/src
COPY backend/pom.xml backend/pom.xml
COPY pom.xml pom.xml
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw \
    && ./mvnw -q -B -N install \
    && ./mvnw -q -B -pl runner-core,runner-service -am install -DskipTests

# --- Imagen final -----------------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk
ARG JUNIT_VERSION=1.12.2

# Python + pytest: aqui SI van los lenguajes, porque este es el servicio que ejecuta.
RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 python3-pip \
    && pip3 install --break-system-packages --no-cache-dir pytest==8.3.4 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# El jar de JUnit que usan los ejercicios de Java.
ADD https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/${JUNIT_VERSION}/junit-platform-console-standalone-${JUNIT_VERSION}.jar /app/runner/junit-platform-console-standalone.jar
RUN chmod 644 /app/runner/junit-platform-console-standalone.jar

# Usuario sin privilegios: el codigo del alumno corre con este usuario.
RUN useradd --system --uid 10001 --no-create-home --shell /usr/sbin/nologin runner
WORKDIR /app
COPY --from=build /build/runner-service/target/*.jar app.jar

ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    JAVALAB_JUNIT_JAR=/app/runner/junit-platform-console-standalone.jar \
    JAVA_OPTS="-Xmx384m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"

USER runner
EXPOSE 8081
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
