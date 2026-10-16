# Colecciones

Python trae de serie las estructuras para agrupar datos: listas, diccionarios y conjuntos.

## Listas

Una lista es una secuencia ordenada y modificable. Se escribe entre corchetes.

```python
numeros = [3, 1, 4, 1, 5]
```

- Acceso por índice (empieza en 0): `numeros[0]`.
- Tamaño con `len(numeros)`.
- Añadir con `numeros.append(9)`.
- Ordenar en el sitio con `numeros.sort()`; ordenar devolviendo una copia con `sorted(numeros)`.
- Recorrer con `for n in numeros:`.
- Suma y máximo con `sum(numeros)` y `max(numeros)`.

## Comprensión de listas

Una forma compacta de construir una lista a partir de otra.

```python
def cuadrados(valores):
    return [n * n for n in valores]
```

## Diccionarios

Asocian una clave con un valor. Se escriben entre llaves.

```python
edades = {"Ana": 30, "Luis": 25}
```

- Leer con `edades["Ana"]` o, para evitar errores, `edades.get("Ana")`.
- Recorrer las claves con `for nombre in edades:` y los pares con `edades.items()`.
- Obtener solo los valores con `edades.values()` (por ejemplo, `sum(edades.values())`).
- Comprobar una clave con `"Ana" in edades`.

## Conjuntos

Un conjunto guarda valores **sin repetir**, muy útil para contar elementos únicos.

```python
def unicos(valores):
    return set(valores)
```

## Colas de prioridad con heapq

El módulo `heapq` trata una lista como un **montículo**: el elemento más pequeño siempre queda al frente. Es la forma estándar de sacar siempre el de mayor prioridad cuando usas números pequeños para lo urgente.

```python
import heapq

def mas_urgente(tareas):
    if not tareas:
        return None
    return heapq.nsmallest(1, tareas)[0]
```
