# Ciclos

Un **ciclo** repite un bloque de código. Python tiene dos: `for` para repetir un número conocido de veces o recorrer una colección, y `while` para repetir mientras se cumpla una condición.

## for con range

`range(inicio, fin)` genera los números desde `inicio` hasta `fin - 1` (el final **no** se incluye).

```python
for i in range(1, 4):
    print(i)   # 1, 2, 3
```

También se puede usar un solo argumento: `range(5)` produce 0, 1, 2, 3 y 4.

## for para recorrer una colección

Cuando recorres una lista, un texto o una tupla, el `for` te da un elemento por vuelta.

```python
def contar_vocales(texto):
    total = 0
    for letra in texto.lower():
        if letra in "aeiou":
            total += 1
    return total
```

## while

`while` repite mientras la condición sea verdadera. Es útil cuando no sabes cuántas vueltas hará falta.

```python
def collatz_steps(n):
    pasos = 0
    while n != 1:
        if n % 2 == 0:
            n = n // 2
        else:
            n = 3 * n + 1
        pasos += 1
    return pasos
```

`//` es la división entera: `n // 2` da el resultado sin decimales.

## Imprimir pirámides con bloques de texto

Para dibujar patrones se combinan `for` y la multiplicación de textos: `"*" * 3` da `"***"` y `" " * 2` da dos espacios.

```python
def build_pyramid(n):
    for i in range(1, n + 1):
        print(" " * (n - i) + "*" * (2 * i - 1))
```

Cada `print(...)` añade un salto de línea automático, así que una llamada por fila produce varias líneas.

## Construir listas

Un ciclo puede ir guardando resultados en una lista con `append(...)`.

```python
def times_table(n):
    filas = []
    for i in range(1, 11):
        filas.append(f"{n} x {i} = {n * i}")
    return filas
```

Las **f-strings** (`f"...{expresión}..."`) insertan el valor de una expresión dentro del texto.
