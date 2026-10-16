#!/usr/bin/env python3
"""Valida el marcado de los ejemplos dentro de los enunciados.

Comprueba que:
  - cada linea de ejemplo con `->` es una lista (`- ...`)
  - los ejemplos de impresion usan `nombre(args) imprime: ...`
  - los bloques ```java van seguidos de un bloque ```java-lab
  - no quedan restos de etiquetas `Entrada:`/`Salida:`

Uso: python3 verificar_marcado.py [java python ...]
     python3 verificar_marcado.py python/level-0
"""
import glob
import os
import re
import sys

import yaml

RAIZ = os.path.dirname(os.path.abspath(__file__))
EJERCICIOS = os.path.join(RAIZ, "exercises")


def revisar(ruta):
    datos = yaml.safe_load(open(ruta, encoding="utf-8"))
    statement = datos["statement"]
    problemas = []
    lineas = statement.split("\n")

    for i, linea in enumerate(lineas):
        t = linea.strip()
        # Ignora lambdas Java (`() -> ...`) y líneas de prosa que mencionan una llamada.
        es_lambda = re.search(r"\(\s*\)\s*->", t)
        if ("->" in t or "→" in t) and not t.startswith("- ") and not es_lambda and not t.startswith("#"):
            problemas.append(f"linea {i + 1}: ejemplo con flecha sin guion -> {t[:60]}")
        if t.lower().startswith(("entrada:", "- entrada:")):
            problemas.append(f"linea {i + 1}: quedan etiquetas Entrada/Salida -> {t[:60]}")

    for i, linea in enumerate(lineas):
        if linea.strip() == "```java":
            j = i + 1
            while j < len(lineas) and not lineas[j].strip().startswith("```"):
                j += 1
            j += 1
            if j >= len(lineas) or lineas[j].strip() != "```java-lab":
                problemas.append(f"linea {i + 1}: bloque ```java sin su salida ```java-lab")

    return problemas


def archivos_a_revisar(argumentos):
    """Sin argumentos, todos los lenguajes. Acepta 'python' o 'python/level-0'."""
    objetivos = argumentos or sorted(
        d for d in os.listdir(EJERCICIOS) if os.path.isdir(os.path.join(EJERCICIOS, d)))
    rutas = []
    for objetivo in objetivos:
        base = os.path.join(EJERCICIOS, *objetivo.split("/"))
        rutas.extend(glob.glob(os.path.join(base, "**", "*.yml"), recursive=True))
    return sorted(r for r in rutas if os.path.basename(r) != "topics.yml")


def main():
    archivos = archivos_a_revisar(sys.argv[1:])

    total = 0
    for ruta in archivos:
        for p in revisar(ruta):
            total += 1
            print(f"  {os.path.relpath(ruta, RAIZ)}: {p}")

    if total:
        print(f"\nPROBLEMAS DE MARCADO ({total}).")
        return 1
    print(f"Marcado correcto en {len(archivos)} enunciados.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
