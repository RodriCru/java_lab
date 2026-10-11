## Qué es

Problemas de razonamiento numérico donde hay que combinar condicionales, ciclos y operadores aritméticos. Lo importante es traducir la regla matemática a pasos que un ciclo pueda repetir.

## Cómo se usa

- Un **acumulador** guarda un resultado parcial: se inicia con el valor neutro (`0` para sumas, `1` para productos) y se actualiza en cada vuelta.
- División entera y residuo: `a / b` y `a % b`.
- Utilidades de `Math`: `Math.max`, `Math.min`, `Math.abs`, `Math.pow` (devuelve `double`), `Math.sqrt`.
- Los tipos tienen límite: `int` llega a unos 2147 millones; si el resultado crece, usa `long`.
- Para fórmulas con muchos casos, define bien el caso más pequeño (por ejemplo, el valor de `0` o `1`) y construye desde ahí.
- Si la operación es repetida y enorme, aplica el módulo en cada paso para que los números no crezcan.

## Ejemplo

```java
// Producto acumulado de 1 a 5
long producto = 1;
for (int i = 1; i <= 5; i++) {
    producto *= i;
}
System.out.println(producto);
// imprime: 120

// Elevar sin Math.pow, con un ciclo
int base = 3, exp = 4;
long resultado = 1;
for (int i = 0; i < exp; i++) {
    resultado *= base;
}
System.out.println(resultado);
// imprime: 81

// Una fila de un triángulo a partir de la anterior
int[] fila = {1, 3, 3, 1};
int[] siguiente = new int[fila.length + 1];
siguiente[0] = 1;
for (int i = 1; i < fila.length; i++) {
    siguiente[i] = fila[i - 1] + fila[i];
}
siguiente[fila.length] = 1;
System.out.println(Arrays.toString(siguiente));
// imprime: [1, 4, 6, 4, 1]
```

## Errores comunes

- Iniciar el acumulador del producto en `0`: todo da `0`.
- Desbordar un `int` sin darse cuenta; el resultado se vuelve negativo.
- Dividir enteros y perder los decimales.
- Olvidar los casos borde: `0`, `1` y números negativos.

## En entrevistas

- Calcular factorial, potencias y sucesiones sin librerías.
- Detectar y evitar desbordamiento con `long` o con el módulo.
- Explicar la complejidad: una potencia por ciclo es O(n); con exponenciación rápida (elevar al cuadrado y dividir el exponente entre 2) baja a O(log n).
