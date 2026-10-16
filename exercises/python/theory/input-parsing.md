# Entrada de datos

En muchos ejercicios y entrevistas los datos llegan como **texto** y hay que convertirlos a números, listas o diccionarios antes de trabajar con ellos. Convertir texto en datos se llama *parsear*.

## Convertir un trozo a número

`int("42")` convierte una cadena a entero y `float("3.14")` a decimal. Si el texto no es un número válido se lanza un `ValueError`.

```python
numero = int("42")      # 42
decimal = float("2.5")  # 2.5
```

## Separar una línea en trozos

`texto.split()` **sin argumentos** separa por cualquier cantidad de espacios en blanco (espacios, tabuladores o saltos de línea) y descarta los vacíos. Con un separador explícito, `texto.split(";")`, corta por ese carácter.

```python
"1 2 3".split()          # ["1", "2", "3"]
"  4   5 ".split()       # ["4", "5"]
"a=1;b=2".split(";")     # ["a=1", "b=2"]
```

## De trozos a una lista de números

Combina `split()` con una comprensión de listas para convertir cada trozo.

```python
def parse_numbers_line(line):
    return [int(trozo) for trozo in line.split()]
```

## Limpiar los espacios sobrantes

`texto.strip()` quita espacios al principio y al final de cada trozo, útil cuando los datos vienen con huecos.

```python
"  a  ".strip()   # "a"
```

## Pares clave-valor

Un texto como `"a=1;b=2"` se convierte en un diccionario separando primero por `;` y luego cada par por `=`. Usa `split("=", 1)` para no romper valores que contengan el signo `=`.

```python
def parse_key_values(text):
    resultado = {}
    for par in text.split(";"):
        clave, valor = par.split("=", 1)
        resultado[clave.strip()] = valor.strip()
    return resultado
```

## Matrices a partir de texto

Si las filas van separadas por `;` y los números de cada fila por espacios, se combinan dos comprensiones: una para las filas y otra para los números de cada fila.

```python
def read_matrix(text):
    return [[int(numero) for numero in fila.split()] for fila in text.split(";")]
```

## Sumar lo que se leyó

Una vez tienes una lista de números, `sum(...)` los suma. Un generador (sin corchetes) evita crear una lista intermedia.

```python
def sum_tokens(line):
    return sum(int(trozo) for trozo in line.split())
```
