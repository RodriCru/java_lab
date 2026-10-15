## Qué es

Una **excepción** es un objeto que representa un problema ocurrido durante la ejecución (dividir entre cero, texto que no es un número, índice fuera de rango). Al lanzarse, el flujo normal se interrumpe y Java busca un bloque que la atrape.

Hay dos familias: las **verificadas** (`Exception` y sus hijas, como `IOException`), que el compilador obliga a atrapar o declarar con `throws`, y las **no verificadas** (`RuntimeException` y sus hijas, como `IllegalArgumentException` o `NullPointerException`), que no obligan nada.

## Cómo se usa

- `try { ... }` encierra el código que puede fallar.
- `catch (Tipo e) { ... }` maneja ese tipo de error. Puedes encadenar varios; el más específico va primero.
- `finally { ... }` se ejecuta siempre, haya o no excepción.
- `throw new X("mensaje")` lanza una excepción; `throws X` en la firma avisa que el método puede lanzarla.
- `try (Recurso r = ...) { ... }` cierra el recurso solo (debe implementar `AutoCloseable`).
- Una excepción propia es una clase que extiende `Exception` o `RuntimeException`.

## Ejemplo

```java
static class SaldoInsuficienteException extends Exception {
    private final int faltante;

    SaldoInsuficienteException(String mensaje, int faltante) {
        super(mensaje);
        this.faltante = faltante;
    }

    int getFaltante() { return faltante; }
}

static int retirar(int saldo, int monto) throws SaldoInsuficienteException {
    if (monto > saldo) {
        throw new SaldoInsuficienteException("No alcanza", monto - saldo);
    }
    return saldo - monto;
}

static void demo() {
    try {
        retirar(100, 150);
    } catch (SaldoInsuficienteException e) {
        System.out.println(e.getMessage() + ", faltan " + e.getFaltante());
        // imprime: No alcanza, faltan 50
    } finally {
        System.out.println("fin");
        // imprime: fin
    }
}
```

Para convertir texto en número se usa `Integer.parseInt`, que lanza `NumberFormatException` si el texto no es válido:

```java
try {
    Integer.parseInt("abc");
} catch (NumberFormatException e) {
    System.out.println("no es número");
    // imprime: no es número
}
```

## Errores comunes

- Atrapar `Exception` para todo y dejar el `catch` vacío: se esconden los errores.
- Poner un `catch` general antes que uno específico: no compila.
- Olvidar que `finally` corre incluso con `return` dentro del `try`.
- Perder la causa original: al relanzar, pásala como segundo argumento (`new X("msg", e)`) y consúltala con `getCause()`.

## En entrevistas

- Diferencia entre excepciones verificadas y no verificadas.
- Diferencia entre `throw` y `throws`.
- Qué pasa con `finally` si hay un `return` en el `try`.
- Para qué sirve try-with-resources y qué interfaz exige.
