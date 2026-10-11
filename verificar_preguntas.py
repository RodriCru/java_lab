#!/usr/bin/env python3
"""Valida el formato del banco de preguntas (questions/*.yml).

Comprueba por pregunta: claves, id único y con el prefijo del tema, dificultad válida,
exactamente 4 opciones distintas, correctIndex entero 0-3 y explicación no vacía.
Por archivo: 25 preguntas y reparto de dificultades/respuestas razonable (solo avisa).
Uso: python3 verificar_preguntas.py [archivo.yml ...]   (sin argumentos: todos)
"""
import collections
import glob
import os
import sys

import yaml

RAIZ = os.path.dirname(os.path.abspath(__file__))
DIFICULTADES = {"easy", "medium", "hard"}
OBLIGATORIAS = ["id", "topic", "difficulty", "question", "options", "correctIndex", "explanation"]


def main():
    archivos = sys.argv[1:] or sorted(glob.glob(os.path.join(RAIZ, "questions", "*.yml")))
    errores, avisos, ids, total = [], [], set(), 0
    for ruta in archivos:
        nombre = os.path.basename(ruta)
        tema = nombre[:-4]
        datos = yaml.safe_load(open(ruta, encoding="utf-8")) or {}
        preguntas = datos.get("questions") or []
        total += len(preguntas)
        if len(preguntas) != 25:
            avisos.append(f"{nombre}: tiene {len(preguntas)} preguntas (se esperaban 25)")
        dif = collections.Counter(p.get("difficulty") for p in preguntas)
        pos = collections.Counter(p.get("correctIndex") for p in preguntas)
        if max(pos.values(), default=0) > 0.4 * max(len(preguntas), 1):
            avisos.append(f"{nombre}: respuestas correctas poco repartidas {dict(pos)}")
        for i, p in enumerate(preguntas, 1):
            et = f"{nombre}#{i}"
            for c in OBLIGATORIAS:
                if p.get(c) is None or p.get(c) == "":
                    errores.append(f"{et}: falta '{c}'")
            if p.get("topic") != tema:
                errores.append(f"{et}: topic '{p.get('topic')}' no coincide con el archivo")
            pid = p.get("id", "")
            if not str(pid).startswith(tema + "-"):
                errores.append(f"{et}: el id '{pid}' debe empezar por '{tema}-'")
            if pid in ids:
                errores.append(f"{et}: id duplicado '{pid}'")
            ids.add(pid)
            if p.get("difficulty") not in DIFICULTADES:
                errores.append(f"{et}: difficulty inválida '{p.get('difficulty')}'")
            op = p.get("options") or []
            if len(op) != 4 or len(set(map(str, op))) != 4:
                errores.append(f"{et}: debe tener 4 opciones distintas")
            idx = p.get("correctIndex")
            if not (isinstance(idx, int) and not isinstance(idx, bool) and 0 <= idx < max(len(op), 1)):
                errores.append(f"{et}: correctIndex inválido '{idx}'")
            if any(str(o).lower().startswith(("todas las anteriores", "ninguna de las anteriores")) for o in op):
                errores.append(f"{et}: no uses 'todas/ninguna de las anteriores'")
        print(f"  {nombre:24} {len(preguntas):3} preguntas | dificultad {dict(dif)} | correcta en {dict(sorted(pos.items()))}")
    print(f"\nTotal: {total} preguntas")
    for a in avisos:
        print("  AVISO:", a)
    if errores:
        print(f"\nERRORES ({len(errores)}):")
        for e in errores:
            print("  -", e)
        return 1
    print("Formato correcto.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
