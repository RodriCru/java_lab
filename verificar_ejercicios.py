#!/usr/bin/env python3
"""Verificacion independiente del catalogo de ejercicios.

Recorre cada carpeta de lenguaje bajo `exercises/` (java, python...) y, para cada YAML:
  1. comprueba que se parsea y tiene las claves obligatorias de su lenguaje
  2. compila el referenceSolution junto con sus tests  -> deben pasar TODOS
  3. compila el starterCode                            -> debe compilar
  4. ejecuta los tests con el starterCode              -> debe fallar al menos uno

No confia en ningun reporte previo: lee los archivos y ejecuta los tests de verdad
(Java con javac + JUnit; Python con pytest).

Uso:
  python3 verificar_ejercicios.py                 # todos los lenguajes
  python3 verificar_ejercicios.py python          # solo un lenguaje
  python3 verificar_ejercicios.py python/level-0  # solo una carpeta (nivel) de un lenguaje
"""
import glob
import os
import re
import shutil
import subprocess
import sys
import tempfile

import yaml

RAIZ = os.path.dirname(os.path.abspath(__file__))
EJERCICIOS_RAIZ = os.path.join(RAIZ, "exercises")
JUNIT = os.path.join(RAIZ, "runner", "junit-platform-console-standalone.jar")

# Claves comunes a todos los lenguajes. `className` solo se exige en Java.
CLAVES_OBLIGATORIAS = [
    "slug", "level", "topic", "title", "statement", "methodSignature",
    "estimatedMinutes", "difficulty", "hints",
    "starterCode", "referenceSolution", "tests",
]

# Prefijo obligatorio del slug por lenguaje (Java no lleva); los slugs son unicos en todo el laboratorio.
PREFIJOS = {"java": "", "python": "py-", "typescript": "ts-", "c": "c-", "cpp": "cpp-"}


# ---------------------------------------------------------------------------
# Java: compila con javac y ejecuta JUnit.
# ---------------------------------------------------------------------------
def verificar_java(codigo, tests):
    """Devuelve (compila, tests_ejecutados, tests_fallidos, detalle)."""
    tmp = tempfile.mkdtemp(prefix="verif-java-")
    try:
        salida = os.path.join(tmp, "out")
        os.makedirs(salida)

        clase = re.search(r"public\s+class\s+(\w+)", codigo)
        nombre = clase.group(1) if clase else "Solution"

        with open(os.path.join(tmp, nombre + ".java"), "w", encoding="utf-8") as f:
            f.write(codigo)
        with open(os.path.join(tmp, "SolutionTest.java"), "w", encoding="utf-8") as f:
            f.write(tests)

        r = subprocess.run(
            ["javac", "-encoding", "UTF-8", "-cp", JUNIT, "-d", salida,
             os.path.join(tmp, nombre + ".java"), os.path.join(tmp, "SolutionTest.java")],
            capture_output=True, text=True, timeout=120,
        )
        if r.returncode != 0:
            return False, 0, 0, (r.stdout + r.stderr).strip()[:600]

        reports = os.path.join(salida, "reports")
        subprocess.run(
            ["java", "-jar", JUNIT, "execute", "--class-path", salida,
             "--select-class", "SolutionTest", "--reports-dir", reports,
             "--details=none", "--disable-ansi-colors"],
            capture_output=True, text=True, timeout=180,
        )
        return True, *_contar_xml(reports)
    except subprocess.TimeoutExpired:
        return False, 0, 0, "timeout"
    finally:
        shutil.rmtree(tmp, ignore_errors=True)


# ---------------------------------------------------------------------------
# Python: ejecuta pytest y lee su XML (mismo esquema que JUnit).
# ---------------------------------------------------------------------------
def verificar_python(codigo, tests):
    tmp = tempfile.mkdtemp(prefix="verif-py-")
    try:
        entorno = dict(os.environ, PYTHONDONTWRITEBYTECODE="1")
        with open(os.path.join(tmp, "solution.py"), "w", encoding="utf-8") as f:
            f.write(codigo)
        with open(os.path.join(tmp, "test_solution.py"), "w", encoding="utf-8") as f:
            f.write(tests)

        r = subprocess.run(
            [sys.executable, "-m", "py_compile", os.path.join(tmp, "solution.py")],
            capture_output=True, text=True, timeout=60, env=entorno,
        )
        if r.returncode != 0:
            return False, 0, 0, (r.stdout + r.stderr).strip()[:600]

        reports = os.path.join(tmp, "reports")
        os.makedirs(reports, exist_ok=True)
        subprocess.run(
            [sys.executable, "-m", "pytest", "test_solution.py",
             f"--junitxml={os.path.join(reports, 'TEST-solution.xml')}",
             "-p", "no:cacheprovider", "-q"],
            cwd=tmp, capture_output=True, text=True, timeout=180, env=entorno,
        )
        return True, *_contar_xml(reports)
    except subprocess.TimeoutExpired:
        return False, 0, 0, "timeout"
    finally:
        shutil.rmtree(tmp, ignore_errors=True)


def _contar_xml(reports):
    """Cuenta tests ejecutados y fallidos leyendo el XML (sirve igual para JUnit y pytest)."""
    ejecutados = fallidos = 0
    for archivo in os.listdir(reports) if os.path.isdir(reports) else []:
        if not archivo.startswith("TEST-") or not archivo.endswith(".xml"):
            continue
        contenido = open(os.path.join(reports, archivo), encoding="utf-8").read()
        # JUnit pone 'tests' en la raiz; pytest en el <testsuite> anidado. Se acepta cualquiera.
        m = re.search(r'<testsuite[^>]*\btests="(\d+)"', contenido) or re.search(r'tests="(\d+)"', contenido)
        if not m or int(m.group(1)) == 0:
            continue
        ejecutados += int(m.group(1))
        for etiqueta in ("failures", "errors"):
            f = re.search(rf'{etiqueta}="(\d+)"', contenido)
            if f:
                fallidos += int(f.group(1))
        break
    return ejecutados, fallidos, ""


VERIFICADORES = {"java": verificar_java, "python": verificar_python}


def lenguajes_a_verificar(argumentos):
    """Sin argumentos, todos los lenguajes con carpeta. Acepta 'python' o 'python/level-0'."""
    disponibles = sorted(
        d for d in os.listdir(EJERCICIOS_RAIZ)
        if os.path.isdir(os.path.join(EJERCICIOS_RAIZ, d)))
    if not argumentos:
        return disponibles
    elegidos = []
    for arg in argumentos:
        lenguaje = arg.split("/")[0]
        if lenguaje not in disponibles:
            print(f"ERROR: no existe la carpeta de lenguaje '{lenguaje}'.")
            sys.exit(2)
        elegidos.append(arg)
    return elegidos


def archivos_del_objetivo(objetivo):
    """'python' -> todos sus YAML; 'python/level-0' -> solo esa carpeta."""
    partes = objetivo.split("/")
    base = os.path.join(EJERCICIOS_RAIZ, *partes)
    if len(partes) > 1:
        return sorted(glob.glob(os.path.join(base, "*.yml")))
    return sorted(glob.glob(os.path.join(base, "**", "*.yml"), recursive=True))


def main():
    objetivos = lenguajes_a_verificar(sys.argv[1:])
    problemas = []
    total_tests = 0
    revisados = 0

    for objetivo in objetivos:
        lenguaje = objetivo.split("/")[0]
        verificar = VERIFICADORES.get(lenguaje)
        if verificar is None:
            print(f"  (se omite '{lenguaje}': aun no hay verificador; el runner todavia no lo soporta)")
            continue

        archivos = [a for a in archivos_del_objetivo(objetivo)
                    if os.path.basename(a) != "topics.yml"]
        print(f"\n=== {lenguaje} ({len(archivos)} ejercicios) ===")

        for archivo in archivos:
            relativo = os.path.relpath(archivo, EJERCICIOS_RAIZ)
            with open(archivo, encoding="utf-8") as f:
                datos = yaml.safe_load(f)
            revisados += 1

            decl = [c for c in CLAVES_OBLIGATORIAS if datos.get(c) is None or datos.get(c) == ""]
            if lenguaje == "java" and not datos.get("className"):
                decl.append("className")
            if decl:
                problemas.append(f"{relativo}: faltan claves {decl}")
                continue

            if datos["level"] >= 2:
                cq = datos.get("complexityQuestion") or {}
                opciones = cq.get("options") or []
                idx = cq.get("correctIndex")
                if not (cq.get("prompt") and len(opciones) >= 2 and isinstance(idx, int)
                        and 0 <= idx < len(opciones) and cq.get("explanation")):
                    problemas.append(f"{relativo}: complexityQuestion invalida o ausente")

            if datos["slug"] != os.path.basename(archivo)[:-4]:
                problemas.append(f"{relativo}: el slug '{datos['slug']}' no coincide con el nombre del archivo")

            prefijo = PREFIJOS.get(lenguaje, "")
            if prefijo and not datos["slug"].startswith(prefijo):
                problemas.append(f"{relativo}: el slug debe empezar por '{prefijo}' (unico entre lenguajes)")
            if lenguaje == "java":
                if datos["className"] != "Solution":
                    problemas.append(f"{relativo}: className deberia ser 'Solution'")
                if not datos["methodSignature"].strip().startswith("public static"):
                    problemas.append(f"{relativo}: el metodo deberia ser public static")
            if datos["methodSignature"].strip() not in datos["starterCode"]:
                problemas.append(f"{relativo}: la firma no aparece en el starterCode")

            # 2. referenceSolution + tests
            ok, eje, fall, detalle = verificar(datos["referenceSolution"], datos["tests"])
            if not ok:
                problemas.append(f"{relativo}: la SOLUCION DE REFERENCIA no compila -> {detalle}")
                continue
            if fall != 0:
                problemas.append(f"{relativo}: la solucion de referencia falla {fall} de {eje} tests")
            if eje < 4:
                problemas.append(f"{relativo}: solo tiene {eje} tests (minimo 4)")
            total_tests += eje

            # 3. starterCode compila
            ok_s, eje_s, fall_s, detalle_s = verificar(datos["starterCode"], datos["tests"])
            if not ok_s:
                problemas.append(f"{relativo}: el STARTER CODE no compila -> {detalle_s}")
            elif fall_s == 0:
                problemas.append(f"{relativo}: con el starter pasan TODOS los tests: los tests no miden nada")

            estado = "OK" if not (fall or not ok or not ok_s or fall_s == 0) else "REVISAR"
            print(f"  {estado:8} {os.path.basename(archivo)[:-4]:32} ref {eje - fall}/{eje} tests | starter falla {fall_s}/{eje_s}")

    print(f"\nEjercicios revisados: {revisados}")
    print(f"Tests ejecutados en el catalogo: {total_tests}")
    if problemas:
        print(f"\nPROBLEMAS ({len(problemas)}):")
        for p in problemas:
            print(f"  - {p}")
        return 1

    print("\nTodos los ejercicios verificados correctamente.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
