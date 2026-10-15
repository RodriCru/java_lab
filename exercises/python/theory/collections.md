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
- Comprobar una clave con `"Ana" in edades`.

## Conjuntos

Un conjunto guarda valores **sin repetir**, muy útil para contar elementos únicos.

```python
def unicos(valores):
    return set(valores)
```
