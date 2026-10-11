## Qué es

La abstracción separa **qué** hace algo de **cómo** lo hace. En Java se logra con clases abstractas e interfaces: definen un contrato que las clases concretas deben cumplir.

## Cómo se usa

- **Interfaz**: solo declara comportamiento. Una clase la cumple con `implements` y puede implementar varias.
- Los métodos `default` de una interfaz traen una implementación por defecto que las clases pueden usar o reemplazar.
- **Clase abstracta**: no se puede instanciar; puede tener campos, constructores, métodos concretos y métodos `abstract` sin cuerpo.
- Una subclase concreta debe implementar todos los métodos abstractos.
- **Método plantilla**: la clase abstracta define el algoritmo completo en un método `final` y deja pasos abstractos para que los rellenen las subclases.
- **Estrategia**: inyectas una interfaz (o una lambda) para cambiar el comportamiento sin tocar la clase que la usa.

## Ejemplo

```java
interface Saludo {
    String nombre();
    default String saludar() { return "Hola, " + nombre(); }
}

abstract class Reporte {
    final String generar() {
        return encabezado() + "|" + cuerpo();
    }
    abstract String cuerpo();
    String encabezado() { return "REPORTE"; }
}

class ReporteVentas extends Reporte {
    String cuerpo() { return "ventas"; }
}

interface Descuento {
    double aplicar(double precio);
}

class Demo {
    static void probar() {
        Saludo s = () -> "Ana";
        System.out.println(s.saludar()); // imprime: Hola, Ana
        System.out.println(new ReporteVentas().generar()); // imprime: REPORTE|ventas

        Descuento mitad = precio -> precio / 2;
        System.out.println(mitad.aplicar(10)); // imprime: 5.0
    }
}
```

## Errores comunes

- Intentar hacer `new` de una clase abstracta o de una interfaz.
- Olvidar implementar un método abstracto: la clase debe declararse también `abstract`.
- Usar herencia de clase cuando una interfaz bastaba; solo se hereda de una clase.
- Reducir la visibilidad al implementar: los métodos de una interfaz son `public`.
- Poner estado en una interfaz: solo admite constantes.

## En entrevistas

- Diferencia entre clase abstracta e interfaz, y cuándo usar cada una.
- Para qué existen los métodos `default` (evolucionar interfaces sin romper código).
- El patrón método plantilla frente al patrón estrategia.
- Qué es una interfaz funcional y su relación con las lambdas.
