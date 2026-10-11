#!/bin/sh
# Compila y ejecuta los tests del ejercicio dentro del contenedor.
#
# Entradas montadas por el backend:
#   /in   (solo lectura)  Solution.java + SolutionTest.java
#   /out  (escritura)     clases compiladas + reportes XML
#
# Codigos de salida:
#   20 -> no compilo
#   0  -> los tests pasaron
#   1  -> algun test fallo
#   2  -> no se encontro la clase de tests

set -u

CLASSES_DIR=/out/classes
REPORTS_DIR=/out/reports

rm -rf "$CLASSES_DIR" "$REPORTS_DIR"
mkdir -p "$CLASSES_DIR" "$REPORTS_DIR"

if ! javac -encoding UTF-8 -cp "$JUNIT_JAR" -d "$CLASSES_DIR" /in/*.java; then
    echo "[javalab] La compilacion fallo."
    exit 20
fi

java -jar "$JUNIT_JAR" execute \
    --class-path "$CLASSES_DIR" \
    --select-class SolutionTest \
    --reports-dir="$REPORTS_DIR" \
    --details=none \
    --disable-ansi-colors \
    --fail-if-no-tests

exit $?
