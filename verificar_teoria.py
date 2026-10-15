#!/usr/bin/env python3
"""Verifica que cada bloque ```java de la teoria de Java compile.

Un bloque se acepta si compila de alguna de estas formas, en este orden:
  1. tal cual (clases, interfaces, enums o records de nivel superior)
  2. como cuerpo de un método main (sentencias sueltas)
  3. como miembros de una clase (métodos y campos sueltos)
Se añade `import java.util.*; import java.math.*;` automáticamente.
Los bloques con la primera línea `// no compila` se omiten (para mostrar errores a propósito).

Solo revisa la teoria de Java (exercises/java/theory): los bloques de otros lenguajes
no los compila javac. Para añadir otro lenguaje con verificador propio, ampliar aqui.

Uso: python3 verificar_teoria.py [tema ...]   (sin argumentos: todos)
"""
import glob
import os
import re
import shutil
import subprocess
import sys
import tempfile

RAIZ = os.path.dirname(os.path.abspath(__file__))
TEORIA = os.path.join(RAIZ, "exercises", "java", "theory")
IMPORTS = "import java.util.*;\nimport java.math.*;\nimport java.util.function.*;\nimport java.util.stream.*;\n"


def compila(fuente, nombre):
    tmp = tempfile.mkdtemp(prefix="teoria-")
    try:
        ruta = os.path.join(tmp, nombre + ".java")
        with open(ruta, "w") as f:
            f.write(fuente)
        r = subprocess.run(["javac", "-d", tmp, ruta], capture_output=True, text=True)
        return r.returncode == 0, r.stderr
    finally:
        shutil.rmtree(tmp, ignore_errors=True)


def intentos(codigo):
    yield IMPORTS + codigo, _nombre(codigo)
    yield IMPORTS + "public class T {\n  public static void main(String[] args) throws Exception {\n" + codigo + "\n  }\n}\n", "T"
    yield IMPORTS + "public class T {\n" + codigo + "\n}\n", "T"


def _nombre(codigo):
    m = re.search(r"public\s+(?:final\s+|abstract\s+)*(?:class|interface|enum|record)\s+(\w+)", codigo)
    if m:
        return m.group(1)
    m = re.search(r"(?m)^(?:final\s+|abstract\s+)*(?:class|interface|enum|record)\s+(\w+)", codigo)
    return "Tmp" if not m else "Tmp"


def main():
    pedidos = set(sys.argv[1:])
    fallos = 0
    total = 0
    for ruta in sorted(glob.glob(os.path.join(TEORIA, "*.md"))):
        tema = os.path.basename(ruta)[:-3]
        if pedidos and tema not in pedidos:
            continue
        texto = open(ruta).read()
        for i, bloque in enumerate(re.findall(r"```java\n(.*?)```", texto, re.S), 1):
            if bloque.lstrip().startswith("// no compila"):
                continue
            total += 1
            ultimo = ""
            ok = False
            for fuente, nombre in intentos(bloque):
                ok, err = compila(fuente, nombre)
                if ok:
                    break
                ultimo = err
            if not ok:
                fallos += 1
                print(f"FALLA {tema} bloque {i}:\n{bloque}\n{ultimo}")
    print(f"{total - fallos}/{total} bloques compilan")
    sys.exit(1 if fallos else 0)


main()
