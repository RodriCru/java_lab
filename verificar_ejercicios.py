#!/usr/bin/env python3
"""Verificacion independiente del catalogo de ejercicios.

Para cada YAML:
  1. comprueba que se parsea y tiene todas las claves obligatorias
  2. compila el referenceSolution junto con sus tests  -> deben pasar TODOS
  3. compila el starterCode                            -> debe compilar
  4. ejecuta los tests con el starterCode              -> debe fallar al menos uno

No confia en ningun reporte previo: lee los archivos y ejecuta JUnit de verdad.
"""
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

CLAVES_OBLIGATORIAS = [
    "slug", "level", "topic", "title", "statement", "methodSignature",
    "className", "estimatedMinutes", "difficulty", "hints",
    "starterCode", "referenceSolution", "tests",
]


def compilar_y_ejecutar(codigo, tests, etiqueta):
    """Devuelve (compila, tests_ejecutados, tests_fallidos, detalle)."""
    tmp = tempfile.mkdtemp(prefix="verif-")
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

        ejecutados = fallidos = 0
        for archivo in os.listdir(reports) if os.path.isdir(reports) else []:
            if not archivo.startswith("TEST-") or not archivo.endswith(".xml"):
                continue
            contenido = open(os.path.join(reports, archivo), encoding="utf-8").read()
            m = re.search(r'tests="(\d+)"', contenido)
            if not m or int(m.group(1)) == 0:
                continue
            ejecutados += int(m.group(1))
            for etiqueta_xml in ("failures", "errors"):
                f = re.search(rf'{etiqueta_xml}="(\d+)"', contenido)
                if f:
                    fallidos += int(f.group(1))
            break

        return True, ejecutados, fallidos, ""
    except subprocess.TimeoutExpired:
        return False, 0, 0, "timeout"
    finally:
        shutil.rmtree(tmp, ignore_errors=True)


def main():
    # Sin argumentos verifica todos los niveles; con "level-2" solo ese directorio.
    niveles = sys.argv[1:] or sorted(
        d for d in os.listdir(EJERCICIOS_RAIZ) if d.startswith("level-"))
    archivos = sorted(
        os.path.join(n, f) for n in niveles
        for f in os.listdir(os.path.join(EJERCICIOS_RAIZ, n)) if f.endswith(".yml"))
    problemas = []
    total_tests = 0
    print(f"Verificando {len(archivos)} ejercicios...\n")

    for archivo in archivos:
        ruta = os.path.join(EJERCICIOS_RAIZ, archivo)
        with open(ruta, encoding="utf-8") as f:
            datos = yaml.safe_load(f)

        # level: 0 es un valor válido (Fundamentos), por eso no se usa "not datos.get(c)".
        faltan = [c for c in CLAVES_OBLIGATORIAS if datos.get(c) is None or datos.get(c) == ""]
        if faltan:
            problemas.append(f"{archivo}: faltan claves {faltan}")
            continue

        if datos["level"] >= 2:
            cq = datos.get("complexityQuestion") or {}
            opciones = cq.get("options") or []
            idx = cq.get("correctIndex")
            if not (cq.get("prompt") and len(opciones) >= 2 and isinstance(idx, int)
                    and 0 <= idx < len(opciones) and cq.get("explanation")):
                problemas.append(f"{archivo}: complexityQuestion invalida o ausente")

        if datos["slug"] != os.path.basename(archivo)[:-4]:
            problemas.append(f"{archivo}: el slug '{datos['slug']}' no coincide con el nombre del archivo")

        if datos["className"] != "Solution":
            problemas.append(f"{archivo}: className deberia ser 'Solution'")

        if not datos["methodSignature"].strip().startswith("public static"):
            problemas.append(f"{archivo}: el metodo deberia ser public static")
        if not datos["methodSignature"].strip().rstrip(";") in datos["starterCode"] + ";":
            if datos["methodSignature"].strip() not in datos["starterCode"]:
                problemas.append(f"{archivo}: la firma no aparece en el starterCode")

        # 2. referenceSolution + tests
        ok, eje, fall, detalle = compilar_y_ejecutar(datos["referenceSolution"], datos["tests"], "ref")
        if not ok:
            problemas.append(f"{archivo}: la SOLUCION DE REFERENCIA no compila -> {detalle}")
            continue
        if fall != 0:
            problemas.append(f"{archivo}: la solucion de referencia falla {fall} de {eje} tests")
        if eje < 4:
            problemas.append(f"{archivo}: solo tiene {eje} tests (minimo 4)")
        total_tests += eje

        # 3. starterCode compila
        ok_s, eje_s, fall_s, detalle_s = compilar_y_ejecutar(datos["starterCode"], datos["tests"], "starter")
        if not ok_s:
            problemas.append(f"{archivo}: el STARTER CODE no compila -> {detalle_s}")
        elif fall_s == 0:
            problemas.append(f"{archivo}: con el starter pasan TODOS los tests: los tests no miden nada")

        estado = "OK" if not (faltan or fall or not ok or not ok_s or fall_s == 0) else "REVISAR"
        print(f"  {estado:8} {archivo[:-4]:30} ref {eje - fall}/{eje} tests | starter falla {fall_s}/{eje_s}")

    print(f"\nTests JUnit en el catalogo: {total_tests}")
    if problemas:
        print(f"\nPROBLEMAS ({len(problemas)}):")
        for p in problemas:
            print(f"  - {p}")
        return 1

    print("\nTodos los ejercicios verificados correctamente.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
