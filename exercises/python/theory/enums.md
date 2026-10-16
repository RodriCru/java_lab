# Enums

Un **Enum** es un tipo con un conjunto fijo de valores con nombre. Se usa cuando una variable solo puede tomar un pequeño número de valores conocidos, como los colores de un semáforo o los días de la semana. Se importa de la librería estándar con `from enum import Enum`.

```python
from enum import Enum


class TrafficLight(Enum):
    ROJO = 1
    AMARILLO = 2
    VERDE = 3
```

Cada nombre (`ROJO`, `AMARILLO`, `VERDE`) es un **miembro** del Enum y se lee como `TrafficLight.ROJO`. El número de la derecha es solo su valor interno; no hay que usarlo directamente.

## Comparar miembros

Los miembros se comparan con `is` (o `==`), nunca con `<` ni `>`.

```python
luz = TrafficLight.ROJO
luz is TrafficLight.ROJO   # True
```

## Atributos .name y .value

Cada miembro conoce su nombre y su valor.

```python
TrafficLight.ROJO.name     # "ROJO"
TrafficLight.ROJO.value    # 1
```

## Recorrer un Enum

Un Enum es iterable: al recorrerlo se visitan los miembros en el orden en que se declararon.

```python
for luz in TrafficLight:
    print(luz.name)   # ROJO, AMARILLO, VERDE
```

## Métodos y propiedades

Un Enum puede tener métodos, que se llaman sobre un miembro. También puede exponer una **propiedad** con `@property`, que se lee sin paréntesis.

```python
class Weekday(Enum):
    LUNES = 1
    MARTES = 2
    MIERCOLES = 3
    JUEVES = 4
    VIERNES = 5
    SABADO = 6
    DOMINGO = 7

    def is_weekend(self):
        return self in (Weekday.SABADO, Weekday.DOMINGO)
```

Dentro de un método, `self` es el miembro sobre el que se llamó: `Weekday.SABADO.is_weekend()` devuelve `True`.

```python
@property
def siguiente(self):
    if self is TrafficLight.ROJO:
        return TrafficLight.VERDE
    return TrafficLight.ROJO
```

Con `@property`, `TrafficLight.ROJO.siguiente` devuelve el miembro siguiente sin escribir paréntesis. Así, un Enum con métodos sirve para modelar una **máquina de estados** sencilla.
