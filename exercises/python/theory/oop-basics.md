# POO: herencia y polimorfismo

Una **clase** es un molde que agrupa datos (atributos) y comportamiento (métodos). Se crea una instancia llamándola como si fuera una función.

```python
class Persona:
    def __init__(self, nombre):
        self.nombre = nombre

    def saludar(self):
        return "Hola, " + self.nombre
```

`__init__` es el constructor: se ejecuta al crear el objeto. El primer parámetro de todo método es `self`, que representa al propio objeto y sirve para acceder a sus atributos.

## Atributos de instancia y de clase

Un atributo de **instancia** se crea con `self.<nombre>` y cada objeto tiene el suyo. Un atributo de **clase** se escribe dentro de la clase pero fuera de los métodos, y lo comparten todas las instancias.

```python
class Widget:
    creados = 0          # atributo de clase

    def __init__(self, nombre):
        self.nombre = nombre       # atributo de instancia
        Widget.creados += 1        # se modifica con el nombre de la clase
```

Para cambiarlo se usa el nombre de la clase (`Widget.creados`), no `self`, porque así se modifica el valor único y compartido.

## Herencia

Una clase hija hereda de otra escribiendo el padre entre paréntesis. Hereda sus métodos y puede **sobrescribirlos** (definir un método con el mismo nombre y otro comportamiento).

```python
class Animal:
    def speak(self):
        return "..."

class Perro(Animal):
    def speak(self):
        return "Guau"
```

`isinstance(objeto, Clase)` comprueba si un objeto pertenece a una clase o a alguna de sus hijas.

## `super()`

`super()` da acceso a la clase padre. Es habitual llamar a su constructor para no repetir la inicialización:

```python
class Vehicle:
    def __init__(self, marca, ruedas):
        self.marca = marca
        self.ruedas = ruedas

    def describe(self):
        return f"{self.marca} con {self.ruedas} ruedas"

class Car(Vehicle):
    def __init__(self, marca, puertas):
        super().__init__(marca, 4)   # reutiliza el constructor del padre
        self.puertas = puertas

    def describe(self):
        return super().describe() + f" y {self.puertas} puertas"
```

## Polimorfismo

El polimorfismo es tratar objetos de clases distintas a través del mismo método. Cada objeto responde con su propia versión, y quien los usa no necesita saber de qué clase son.

```python
animales = [Perro(), Gato()]
for animal in animales:
    print(animal.speak())   # cada uno con su sonido
```

## Métodos especiales: `__str__` y `__eq__`

Los métodos con doble guion bajo al principio y al final ("dunder") definen comportamientos del lenguaje. `__str__` decide cómo se convierte el objeto en texto con `str(...)` o dentro de una f-string.

```python
class Person:
    def __init__(self, nombre, edad):
        self.nombre = nombre
        self.edad = edad

    def __str__(self):
        return f"{self.nombre} ({self.edad} años)"
```

`__eq__` define cuándo dos objetos son iguales con `==`. Sin él, `==` compara si son el mismo objeto en memoria; con él, puedes comparar por sus atributos.

```python
    def __eq__(self, otro):
        return self.nombre == otro.nombre and self.edad == otro.edad
```
