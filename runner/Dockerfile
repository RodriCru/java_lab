# Imagen de ejecucion del laboratorio.
#
# Compila el codigo del alumno junto con los tests JUnit del ejercicio y ejecuta los tests.
# El aislamiento NO vive aqui: lo aplica el backend al lanzar el contenedor
# (--network none, --read-only, --cap-drop ALL, limites de memoria y CPU, timeout).
FROM eclipse-temurin:17-jdk

ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -XX:+UseSerialGC -Xss512k" \
    JUNIT_JAR=/opt/junit/junit-platform-console-standalone.jar

# Los tests JUnit ya vienen en la imagen: asi cada envio no descarga nada.
COPY junit-platform-console-standalone.jar /opt/junit/junit-platform-console-standalone.jar
COPY entrypoint.sh /usr/local/bin/javalab-entrypoint

RUN chmod 0555 /usr/local/bin/javalab-entrypoint \
    && mkdir -p /in /out \
    && chmod 0777 /out

WORKDIR /out

# 20 = error de compilacion. El backend lo usa para distinguirlo de un test fallido.
ENTRYPOINT ["/usr/local/bin/javalab-entrypoint"]
