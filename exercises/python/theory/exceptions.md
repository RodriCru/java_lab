# Excepciones

Una **excepción** es un error que interrumpe el flujo normal del programa. En Python se **capturan** con `try` / `except` en lugar de comprobando códigos de error.

## Capturar un error

El código que puede fallar va dentro de `try`; si ocurre una excepción, se ejecuta el bloque `except`.

```python
def dividir(a, b):
    try:
        return a / b
    except ZeroDivisionError:
        return None
```

Se puede capturar más de un tipo a la vez con una tupla: `except (ValueError, TypeError):`.

## La cláusula else

El bloque `else` se ejecuta solo si **no** hubo excepción. Es útil para separar el código que puede fallar del que no.

```python
def convertir(texto):
    try:
        valor = int(texto)
    except ValueError:
        return None
    else:
        return valor * 2
```

## La cláusula finally

`finally` se ejecuta **siempre**, haya excepción o no. Sirve para liberar recursos (cerrar archivos, conexiones).

```python
def leer(archivo):
    try:
        return archivo.read()
    finally:
        archivo.close()
```

El orden de ejecución es: `try` → (`except` en caso de error, o `else` si todo va bien) → `finally`.

## Lanzar una excepción

Con `raise` se lanza una excepción. Se suele usar para señalar que los datos recibidos no son válidos.

```python
def validar_edad(edad):
    if edad < 0:
        raise ValueError("La edad no puede ser negativa")
    return edad
```

## Excepciones propias

Una excepción propia se crea **heredando** de `Exception`. Así el código que llama puede distinguir ese error de otros.

```python
class SaldoInsuficienteError(Exception):
    pass


def retirar(saldo, monto):
    if monto > saldo:
        raise SaldoInsuficienteError("Saldo insuficiente")
    return saldo - monto
```

## Encadenar excepciones

`raise ... from ...` indica que una excepción fue la **causa** de otra. La original queda accesible en `__cause__`.

```python
def cargar(texto):
    try:
        return int(texto)
    except ValueError as error:
        raise RuntimeError("No se pudo leer el número") from error
```
