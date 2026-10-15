## Qué es

Spring se apoya en dos ideas que se pueden entender sin ninguna librería.

La **inversión de control (IoC)** significa que una clase no crea sus propias dependencias ni decide cuándo se construyen; alguien externo se las entrega. La **inyección de dependencias (DI)** es la técnica concreta: las dependencias llegan desde fuera, normalmente por el constructor.

El beneficio es que las clases dependen de **interfaces**, no de implementaciones concretas, así que son fáciles de cambiar y de probar con versiones falsas.

## Cómo se usa

- Define una interfaz para lo que la clase necesita.
- Recíbela como parámetro del constructor y guárdala en un campo `final`.
- Quien arma la aplicación (el "contenedor" o el `main`) decide qué implementación entregar.
- Un **singleton** es una única instancia compartida; es el alcance por defecto de los beans de Spring.

## Ejemplo

```java
interface Notificador {
    String enviar(String mensaje);
}

static class CorreoNotificador implements Notificador {
    public String enviar(String mensaje) { return "correo: " + mensaje; }
}

static class Pedidos {
    private final Notificador notificador;

    Pedidos(Notificador notificador) {
        this.notificador = notificador;
    }

    String confirmar(int id) {
        return notificador.enviar("pedido " + id);
    }
}

static void demo() {
    Notificador n = new CorreoNotificador();
    Pedidos pedidos = new Pedidos(n);
    System.out.println(pedidos.confirmar(7));
    // imprime: correo: pedido 7

    // En una prueba se inyecta un doble sin tocar Pedidos
    Pedidos prueba = new Pedidos(m -> "falso: " + m);
    System.out.println(prueba.confirmar(1));
    // imprime: falso: pedido 1
}
```

## Qué hace Spring por ti

- Mantiene un contenedor (el `ApplicationContext`) que crea los objetos, llamados **beans**.
- Detecta qué necesita cada constructor y entrega la implementación correspondiente.
- Gestiona el alcance: por defecto un solo objeto compartido (singleton).
- Tú solo declaras las clases con anotaciones como `@Component` o `@Service`; el armado manual de arriba lo hace él.

## Errores comunes

- Crear la dependencia con `new` dentro de la clase: se pierde el desacoplamiento.
- Depender de la clase concreta en lugar de la interfaz.
- Guardar estado mutable en un singleton compartido entre usuarios.
- Dependencias circulares: A necesita B y B necesita A.

## En entrevistas

- Diferencia entre IoC y DI.
- Por qué se prefiere la inyección por constructor (campos `final`, fácil de probar).
- Qué es un bean y cuál es su alcance por defecto.
- Cómo facilita DI las pruebas unitarias.
