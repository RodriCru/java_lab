## Qué es

Los primeros pasos son las herramientas básicas de todo programa: variables, condicionales y ciclos. Con ellas resuelves la mayoría de problemas de calentamiento.

## Cómo se usa

- Variables tipadas: `int edad = 20;`, `double precio = 9.5;`, `boolean activo = true;`, `String nombre = "Ana";`.
- Condicional: `if (cond) { ... } else if (otra) { ... } else { ... }`.
- Comparaciones: `==`, `!=`, `<`, `<=`, `>`, `>=`. Se combinan con `&&` (y), `||` (o) y `!` (no).
- Ciclo `for` cuando sabes cuántas vueltas: `for (int i = 0; i < n; i++)`.
- Ciclo `while` cuando depende de una condición.
- El operador **módulo** `%` da el residuo: `7 % 2` es `1`. Sirve para saber si algo es par (`n % 2 == 0`).
- Los textos se comparan con `.equals`, no con `==`.

## Ejemplo

```java
int contador = 0;
int suma = 0;
for (int i = 1; i <= 10; i++) {
    if (i % 2 == 0) {
        contador++;
        suma += i;
    }
}
System.out.println(contador + " pares, suma " + suma);
// imprime: 5 pares, suma 30

int a = 4, b = 9, c = 6;
int mayor = a;
if (b > mayor) mayor = b;
if (c > mayor) mayor = c;
System.out.println(mayor);
// imprime: 9
```

## Errores comunes

- Usar `=` (asignación) donde va `==` (comparación).
- Dividir enteros esperando decimales: `7 / 2` da `3`. Usa `7 / 2.0` para `3.5`.
- Errores por uno en el límite del ciclo: `<` contra `<=`.
- Ciclos infinitos por olvidar actualizar la variable de control.
- Comparar `String` con `==` en lugar de `.equals`.
- Poner `;` justo después del `if` o del `for`.

## En entrevistas

- Calentamientos tipo "par o impar", "mayor de tres", "tabla de multiplicar".
- Qué diferencia hay entre `for`, `while` y `do-while`.
- Cómo funcionan los operadores `&&` y `||` con cortocircuito: si el primer operando ya decide, el segundo no se evalúa.
