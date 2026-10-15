## Qué es

Tema de múltiplos, divisores, máximo común divisor (MCD), mínimo común múltiplo (MCM) y números primos. Casi todo se resuelve con el operador **módulo** `%`: si `a % b == 0`, entonces `a` es divisible entre `b`.

Un número **primo** es mayor que 1 y solo es divisible entre 1 y él mismo.

## Cómo se usa

- Múltiplo: `n % k == 0`.
- Divisores de `n`: prueba `i` de `1` a `n` (o hasta `n / 2`) y revisa `n % i == 0`.
- Para saber si `n` es primo basta probar divisores hasta la raíz cuadrada: `i * i <= n`. Si ninguno divide a `n`, es primo.
- **MCD** con el algoritmo de Euclides: repetir `(a, b) -> (b, a % b)` hasta que `b` sea `0`; el resultado es `a`.
- **MCM** se obtiene del MCD: `a / mcd * b` (dividir antes de multiplicar evita desbordar).
- Para varias reglas a la vez (múltiplo de 3 y de 5), comprueba primero el caso combinado.

## Ejemplo

```java
static boolean divisible(int n, int k) {
    return n % k == 0;
}

static int mcd(int a, int b) {
    while (b != 0) {
        int resto = a % b;
        a = b;
        b = resto;
    }
    return a;
}

static void demo() {
    System.out.println(divisible(12, 4));
    // imprime: true
    System.out.println(mcd(48, 18));
    // imprime: 6
    System.out.println(48 / mcd(48, 18) * 18);
    // imprime: 144
}
```

## Errores comunes

- Tratar el `1` (o el `0`) como primo.
- Probar divisores hasta `n` en lugar de hasta la raíz: funciona, pero es lento.
- Dividir entre cero: `n % 0` lanza `ArithmeticException`.
- En reglas combinadas, evaluar primero el caso simple y nunca llegar al combinado.

## En entrevistas

- Escribir `esPrimo` y justificar por qué basta llegar a la raíz cuadrada.
- Explicar el algoritmo de Euclides y su relación con el MCM.
- Generar todos los primos hasta `n` y comentar la criba de Eratóstenes como mejora.
