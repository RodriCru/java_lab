# Imagen de ejecucion de Python del laboratorio.
#
# Ejecuta el codigo del alumno junto con los tests pytest del ejercicio y genera un XML
# compatible con JUnit (mismo esquema que el motor de Java, asi el backend lo lee igual).
# El aislamiento NO vive aqui: lo aplica el backend al lanzar el contenedor
# (--network none, --read-only, --cap-drop ALL, limites de memoria y CPU, timeout).
FROM python:3.12-slim

ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PYTHONHASHSEED=0

# pytest y su plugin de reporte JUnit vienen en la imagen: cada envio no descarga nada.
RUN pip install --no-cache-dir pytest==8.3.4 \
    && mkdir -p /in /out \
    && chmod 0777 /out

COPY entrypoint.sh /usr/local/bin/javalab-entrypoint
RUN chmod 0555 /usr/local/bin/javalab-entrypoint

WORKDIR /out

# 20 = error de compilacion/sintaxis. El backend lo usa para distinguirlo de un test fallido.
ENTRYPOINT ["/usr/local/bin/javalab-entrypoint"]
