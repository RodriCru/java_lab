## Qué es

Un `enum` define un conjunto **fijo** de valores con nombre, como días de la semana o estados de un semáforo. Es más seguro que usar enteros o cadenas sueltas, porque el compilador rechaza valores que no existen.

## Cómo se usa

- Declaración: `enum Color { ROJO, VERDE, AZUL }`.
- Cada valor es un objeto único; se compara con `==`.
- `Color.values()` devuelve un arreglo con todos, `Color.valueOf("ROJO")` busca por nombre y `c.name()` y `c.ordinal()` dan nombre y posición.
- Puede tener **campos, constructor y métodos**. El constructor es implícitamente privado.
- Cada constante puede sobrescribir un método, útil para máquinas de estados: un método `siguiente()` devuelve el estado que sigue.
- Funciona en `switch` sin escribir el prefijo del tipo en los `case`.

## Ejemplo

```java
enum Semaforo {
    ROJO(30), VERDE(25), AMARILLO(5);

    private final int segundos;

    Semaforo(int segundos) { this.segundos = segundos; }

    int getSegundos() { return segundos; }

    Semaforo siguiente() {
        return switch (this) {
            case ROJO -> VERDE;
            case VERDE -> AMARILLO;
            case AMARILLO -> ROJO;
        };
    }
}

class Prueba {
    static void probar() {
        Semaforo s = Semaforo.ROJO;
        System.out.println(s.siguiente()); // imprime: VERDE
        System.out.println(s.getSegundos()); // imprime: 30
        System.out.println(Semaforo.valueOf("AMARILLO").ordinal()); // imprime: 2
        for (Semaforo x : Semaforo.values()) {
            System.out.print(x.name().charAt(0)); // imprime: RVA
        }
    }
}
```

## Errores comunes

- `valueOf` con un nombre inexistente lanza `IllegalArgumentException`.
- Depender de `ordinal()` para lógica de negocio: cambia si reordenas las constantes.
- Intentar crear instancias con `new`: no se puede.
- Olvidar el punto y coma tras la lista de constantes cuando hay campos o métodos.
- Comparar con `equals` por costumbre: funciona, pero `==` es lo idiomático y no falla con `null` a la izquierda.

## En entrevistas

- Por qué un enum es un buen Singleton y es seguro frente a serialización y reflexión.
- Ventajas sobre constantes `int` o `String` (tipado, legibilidad, métodos).
- `EnumMap` y `EnumSet`: colecciones rápidas para claves enum.
- Un enum puede implementar interfaces pero no extender clases.
