#!/bin/sh
# Ejecuta los tests pytest del ejercicio dentro del contenedor.
#
# Entradas montadas por el backend:
#   /in   (solo lectura)  solution.py + test_solution.py
#   /out  (escritura)     reporte XML de JUnit
#
# Codigos de salida:
#   20 -> no compilo (error de sintaxis en el codigo del alumno)
#   0  -> los tests pasaron
#   1  -> algun test fallo

set -u

REPORTS_DIR=/out/reports

rm -rf "$REPORTS_DIR"
mkdir -p "$REPORTS_DIR"

# Comprobacion de sintaxis aparte para reportar el error como compilacion (y no como test fallido).
# /in es de solo lectura: se compila a un directorio temporal escribible.
if ! python -c "import py_compile, sys; py_compile.compile('/in/solution.py', cfile='/tmp/solution.pyc', doraise=True)"; then
    echo "[javalab] La compilacion fallo."
    exit 20
fi

# pytest importa solution.py desde el mismo directorio de los tests, que esta en /in (solo lectura).
cd /in || exit 20

PYTHONDONTWRITEBYTECODE=1 python -m pytest test_solution.py \
    --junitxml="$REPORTS_DIR/TEST-solution.xml" \
    -p no:cacheprovider \
    -q

exit $?
