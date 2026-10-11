## Qué es

Una función es **recursiva** cuando se llama a sí misma para resolver una versión más pequeña del mismo problema. Toda recursión necesita dos piezas: un **caso base** que detiene las llamadas y un **paso recursivo** que se acerca a ese caso.

## Cómo se usa

- Primero escribe el caso base (por ejemplo, `n == 0`, o un texto vacío).
- Luego define el paso: resuelve un trozo pequeño y delega el resto a la misma función.
- Piensa en la forma "supongo que la función ya funciona para `n - 1`; ¿cómo uso eso para `n`?".
- Cada llamada ocupa espacio en la **pila**; demasiadas llamadas provocan `StackOverflowError`.
- Si una función se repite con los mismos argumentos (como Fibonacci), puedes guardar resultados ya calculados en un `Map` o arreglo (memoización).
- Muchas recursiones se pueden escribir también con un ciclo.

## Ejemplo

```java
static int sumaHasta(int n) {
    if (n == 0) return 0;          // caso base
    return n + sumaHasta(n - 1);   // paso recursivo
}

static String invertir(String s) {
    if (s.isEmpty()) return s;
    return invertir(s.substring(1)) + s.charAt(0);
}

static void demo() {
    System.out.println(sumaHasta(4));
    // imprime: 10
    System.out.println(invertir("hola"));
    // imprime: aloh
}
```

La secuencia de llamadas de `sumaHasta(3)` es `3 + (2 + (1 + 0))`, que da `6`.

## Errores comunes

- Olvidar el caso base: recursión infinita y `StackOverflowError`.
- Un paso que no se acerca al caso base (por ejemplo, llamar con `n` en lugar de `n - 1`).
- Casos base incompletos: Fibonacci necesita dos.
- Fibonacci recursivo ingenuo: crece de forma exponencial, O(2^n).
- No considerar valores negativos como entrada.

## En entrevistas

- Factorial, Fibonacci, potencia e inversión de cadenas de forma recursiva.
- Explicar la pila de llamadas y cuándo ocurre un `StackOverflowError`.
- Comparar recursión contra iteración y mencionar memoización.
- Complejidad de una recursión: contar cuántas llamadas se hacen y cuánto trabajo hace cada una.
