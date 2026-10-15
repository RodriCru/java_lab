#!/usr/bin/env bash
# Construye las imagenes Docker que ejecutan el codigo de los alumnos (una por lenguaje).
#
# Uso:  ./build.sh [imagen-java] [imagen-python]
# Por defecto: javalab-runner:17 y javalab-runner-python:3.12
set -euo pipefail

cd "$(dirname "$0")"

IMAGE_JAVA="${1:-javalab-runner:17}"
IMAGE_PYTHON="${2:-javalab-runner-python:3.12}"
JUNIT_VERSION="1.12.2"
JUNIT_JAR="junit-platform-console-standalone.jar"

if ! command -v docker >/dev/null 2>&1; then
    echo "ERROR: no se encontro el comando 'docker'." >&2
    exit 1
fi

# --- Java ---
if [ ! -f "$JUNIT_JAR" ]; then
    echo "Descargando JUnit Platform Console Standalone ${JUNIT_VERSION}..."
    curl -sSL -o "$JUNIT_JAR" \
        "https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/${JUNIT_VERSION}/junit-platform-console-standalone-${JUNIT_VERSION}.jar"
fi

echo "Construyendo la imagen de Java: ${IMAGE_JAVA}..."
docker build -t "$IMAGE_JAVA" .
echo "Listo: ${IMAGE_JAVA}"

# --- Python ---
echo "Construyendo la imagen de Python: ${IMAGE_PYTHON}..."
docker build -t "$IMAGE_PYTHON" python/
echo "Listo: ${IMAGE_PYTHON}"

