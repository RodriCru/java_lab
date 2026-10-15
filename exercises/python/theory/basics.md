# Lo más básico

En Python, una **función** se define con `def`, seguido del nombre, los parámetros entre paréntesis y dos puntos. El cuerpo va indentado (normalmente cuatro espacios) y el resultado se devuelve con `return`.

```python
def saludar(nombre):
    return "Hola, " + nombre
```

A diferencia de Java, **no hay que declarar tipos**: Python los deduce solo. Tampoco hace falta envolver todo en una clase; las funciones viven sueltas en el archivo.

## Variables

Se crean asignando un valor a un nombre. No se declara el tipo.

```python
edad = 30
precio = 9.99
activo = True
nombre = "Ana"
```

## Devolver un resultado

`return` termina la función y entrega un valor. Si una función no tiene `return`, devuelve `None`.

```python
def sumar(a, b):
    return a + b
```

## Condicionales

Se escriben con `if`, `elif` y `else`, y el cuerpo va indentado.

```python
def clasificar(n):
    if n < 0:
        return "negativo"
    elif n == 0:
        return "cero"
    else:
        return "positivo"
```

## Imprimir frente a devolver

`print(...)` muestra un texto en la consola, pero **no** devuelve nada útil: el valor que entrega es `None`. Para que un test pueda comprobar el resultado, hay que usar `return`.

```python
def doble(n):
    print(n * 2)   # solo lo muestra
    return n * 2   # esto es lo que el test puede verificar
```
