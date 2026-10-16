# Clases abstractas y protocolos

Una **clase abstracta** define un contrato: qué métodos deben existir, sin decir cómo se implementan. No se puede instanciar; solo sirve como base para otras clases.

## `ABC` y `@abstractmethod`

Se hereda de `ABC` (del módulo `abc`) y los métodos obligatorios se decoran con `@abstractmethod`.

```python
from abc import ABC, abstractmethod

class Vehicle(ABC):
    def __init__(self, nombre):
        self.nombre = nombre

    @abstractmethod
    def cost(self):
        ...
```

Una clase hija **debe** implementar todos los métodos abstractos. Si no lo hace, tampoco se puede instanciar y Python lanza un `TypeError`.

```python
class Bicycle(Vehicle):
    def cost(self):
        return 0
```

## Métodos concretos junto a los abstractos

La clase abstracta puede mezclar métodos con cuerpo (concretos) y abstractos. El método concreto es código compartido que las hijas heredan tal cual.

## Patrón método plantilla

El patrón **método plantilla** fija en la clase base el esqueleto de un algoritmo y deja los pasos variables a las subclases. El método concreto llama a los abstractos en un orden definido.

```python
class Beverage(ABC):
    def prepare(self):                       # esqueleto fijo
        pasos = [self.boil_water(), self.brew(), self.pour()]
        return " | ".join(pasos)

    def boil_water(self):                    # paso común
        return "Hervir agua"

    @abstractmethod
    def brew(self):                          # paso variable
        ...

    def pour(self):
        return "Servir en taza"
```

Así el orden no se puede romper: cada subclase solo rellena lo que cambia.

## Patrón estrategia

El patrón **estrategia** saca el cálculo variable a un objeto aparte con un método conocido. La clase que lo usa (el contexto) delega en él y puede cambiarlo en cualquier momento.

```python
class Checkout:
    def __init__(self, estrategia):
        self.estrategia = estrategia

    def total(self, amount):
        return self.estrategia.apply(amount)

class PercentDiscount:
    def __init__(self, percent):
        self.percent = percent

    def apply(self, amount):
        return amount - amount * self.percent / 100
```

`Checkout` no sabe qué descuento aplica; solo sabe que cualquier estrategia tiene `apply`.

## Protocolos y duck typing

`typing.Protocol` describe un contrato **por la forma**, sin necesidad de heredar. Si un objeto tiene el método que el protocolo pide, lo cumple: eso es el *duck typing* ("si camina como un pato y grazna como un pato, es un pato").

```python
from typing import Protocol

class Shape(Protocol):
    def area(self):
        ...

class Square:            # no hereda de Shape
    def __init__(self, lado):
        self.lado = lado

    def area(self):
        return self.lado ** 2

def total_area(shapes):
    return sum(s.area() for s in shapes)
```

`Square` sirve aunque no herede de `Shape`: basta con que tenga el método `area`. Los protocolos se usan sobre todo para anotar tipos, en tiempo de ejecución Python no comprueba que se cumplan.
