# Principios de inyección de dependencias

La **inversión de control** (IoC) y la **inyección de dependencias** (DI) son ideas para que una clase no construya sus propias dependencias, sino que las **reciba** desde fuera. En Python no hace falta un framework (como Spring en Java): basta pasar los objetos como parámetros.

## El problema: acoplamiento

Si una clase crea ella misma sus colaboradores, queda atada a esas clases concretas y es difícil de probar o cambiar.

```python
class ServicioDeUsuarios:
    def __init__(self):
        self.repositorio = RepositorioEnMySQL()   # dependencia fija
```

## Inyección por constructor

La solución es recibir la dependencia como **parámetro** del constructor (`__init__`). La clase depende del objeto que recibe, no de cómo se construye.

```python
class ServicioDeUsuarios:
    def __init__(self, repositorio):
        self.repositorio = repositorio

    def registrar(self, nombre):
        self.repositorio.guardar(nombre)
```

Así, en un test se puede inyectar un repositorio sencillo en memoria:

```python
servicio = ServicioDeUsuarios(RepositorioEnMemoria())
```

## Contenedor de dependencias

Un **contenedor** es un registro de **fábricas** que sabe construir objetos y resolver sus dependencias. Se registra un nombre con una función que recibe el propio contenedor.

```python
contenedor = Contenedor()
contenedor.registrar("doble", lambda c: 2)
contenedor.registrar("cuadruple", lambda c: c.resolver("doble") * 2)
```

## Ciclo de vida: transitorio y singleton

- **Transitorio**: cada vez que se resuelve, se crea un objeto nuevo.
- **Singleton**: se crea una sola vez y siempre se devuelve la **misma** instancia.

```python
class Registro:
    _instancia = None

    def __new__(cls):
        if cls._instancia is None:
            cls._instancia = super().__new__(cls)
        return cls._instancia
```

## Patrón observador

El patrón **observador** desacopla quien emite un evento de quien lo escucha: los interesados se **suscriben** y el emisor solo **publica**.

```python
class PublicadorDeEventos:
    def __init__(self):
        self._suscriptores = {}

    def suscribir(self, evento, manejador):
        self._suscriptores.setdefault(evento, []).append(manejador)

    def publicar(self, evento, datos=None):
        for manejador in self._suscriptores.get(evento, []):
            manejador(datos)
```
