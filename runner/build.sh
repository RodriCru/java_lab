#!/usr/bin/env bash
# Construye la imagen Docker que ejecuta el codigo de los alumnos.
#
# Uso:  ./build.sh [nombre-de-imagen]
# Por defecto: javalab-runner:17
set -euo pipefail

cd "$(dirname "$0")"

IMAGE="${1:-javalab-runner:17}"
JUNIT_VERSION="1.12.2"
JUNIT_JAR="junit-platform-console-standalone.jar"

if ! command -v docker >/dev/null 2>&1; then
    echo "ERROR: no se encontro el comando 'docker'." >&2
    exit 1
fi

if [ ! -f "$JUNIT_JAR" ]; then
    echo "Descargando JUnit Platform Console Standalone ${JUNIT_VERSION}..."
    curl -sSL -o "$JUNIT_JAR" \
        "https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/${JUNIT_VERSION}/junit-platform-console-standalone-${JUNIT_VERSION}.jar"
fi

echo "Construyendo la imagen ${IMAGE}..."
docker build -t "$IMAGE" .
echo "Listo: ${IMAGE}"
