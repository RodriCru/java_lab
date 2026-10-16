# Operadores y decisiones

Los programas toman decisiones con **condicionales**. En Python se escriben con `if`, `elif` y `else`, y el cuerpo de cada rama va indentado.

```python
def clasificar(n):
    if n > 0:
        return "positivo"
    elif n < 0:
        return "negativo"
    else:
        return "cero"
```

El primer bloque cuya condición sea verdadera se ejecuta; los demás se ignoran. Por eso conviene **ordenar los casos de más específico a más general**.

## Operadores de comparación

Comparan dos valores y devuelven un booleano (`True` o `False`).

- `==` igual que
- `!=` distinto de
- `<` menor que
- `>` mayor que
- `<=` menor o igual que
- `>=` mayor o igual que

```python
es_mayor = 18 >= 18   # True
es_distinto = 3 != 4  # True
```

## Operadores lógicos

Combinan varias condiciones.

- `and` es verdadero solo si **ambas** condiciones lo son.
- `or` es verdadero si **al menos una** lo es.
- `not` invierte el valor de una condición.

```python
def es_bisiesto(anio):
    return anio % 4 == 0 and (anio % 100 != 0 or anio % 400 == 0)
```

## El operador módulo

`%` devuelve el **resto** de una división. Sirve para saber si un número es divisible por otro: `anio % 4 == 0` es cierto cuando el año es múltiplo de cuatro.

```python
print(10 % 3)  # 1
print(8 % 2)   # 0
```

## Cadenas y comparaciones

Las cadenas se comparan con `==` (igual que) y se pueden buscar dentro de una tupla con `in`.

```python
def es_fin_de_semana(dia):
    return dia in ("sábado", "domingo")
```

## Cuidado con las tildes

En español las tildes importan: `"sábado"` y `"sabado"` son textos distintos en Python. Compara siempre con el texto exacto que espera el enunciado.
