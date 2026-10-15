# Cadenas

Una cadena (`str`) es una secuencia de caracteres entre comillas simples o dobles. Se pueden recorrer, cortar y transformar con sus métodos.

```python
nombre = "Ana"
saludo = 'Hola'
```

## Operaciones básicas

- Concatenar con `+`: `"Hola, " + nombre`.
- Repetir con `*`: `"ab" * 3` da `"ababab"`.
- Longitud con `len(s)`.
- Pertenencia con `in`: `"a" in "Ana"`.

## Métodos útiles

- `s.upper()` y `s.lower()`: a mayúsculas o minúsculas.
- `s.strip()`: quita espacios al principio y al final.
- `s.split(sep)`: divide en una lista de trozos.
- `s.replace(viejo, nuevo)`: reemplaza todas las apariciones.
- `s.startswith(x)` y `s.endswith(x)`: comprueban prefijo o sufijo.

```python
def normalizar(texto):
    return texto.strip().lower()
```

## f-strings

La forma más cómoda de mezclar texto y valores: se pone una `f` delante de las comillas y las variables entre llaves.

```python
def saludo(nombre, edad):
    return f"{nombre} tiene {edad} años"
```

## Recorrer una cadena

Con un `for` se recorren sus caracteres uno a uno.

```python
def invertir(texto):
    resultado = ""
    for caracter in texto:
        resultado = caracter + resultado
    return resultado
```
