# Funciones y argumentos

Una **función** es un bloque con nombre que recibe datos, hace un trabajo y devuelve un resultado. Se define con `def` y sus datos de entrada son los **parámetros**.

```python
def saludar(nombre):
    return "Hola, " + nombre
```

## Parámetros y argumentos

Los **parámetros** son los nombres que aparecen entre paréntesis en la definición; los **argumentos** son los valores que se pasan al llamar.

```python
def restar(a, b):
    return a - b

restar(10, 4)   # a = 10, b = 4
```

Si una función recibe varios valores, los devuelve juntos con una **tupla**:

```python
def min_max(valores):
    return min(valores), max(valores)

menor, mayor = min_max([3, 1, 9])
```

## Valores por defecto

Un parámetro puede traer un **valor por defecto**. Solo se puede omitir ese argumento si no lo pasas:

```python
def area(base, height=1):
    return base * height

area(5)        # height vale 1 -> 5
area(5, 3)     # height vale 3 -> 15
```

Los parámetros con valor por defecto van **después** de los obligatorios.

## Argumentos variables con *args

`*args` recoge todos los argumentos posicionales que sobren en una **tupla**. Sirve cuando no sabes cuántos valores llegarán.

```python
def sum_all(*numbers):
    return sum(numbers)

sum_all()            # 0
sum_all(1, 2, 3)     # 6
```

Dentro de la función, `numbers` es una tupla normal: puedes recorrerla con `for` o pasarla a `sum(...)`, `len(...)`, etc.

## Pasar listas: copia frente a referencia

Una lista se pasa **por referencia**: si la modificas dentro de la función, el llamador ve el cambio. Si no quieres tocar la original, trabaja sobre una copia.

```python
def doblar_todo(valores):
    copia = list(valores)          # copia independiente
    for i in range(len(copia)):
        copia[i] = copia[i] * 2
    return copia
```

`list(valores)` crea una lista nueva con los mismos elementos, pero la original queda intacta.
