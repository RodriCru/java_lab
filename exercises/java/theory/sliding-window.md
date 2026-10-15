## Qué es

La ventana deslizante mantiene un rango contiguo `[left, right]` del arreglo y lo desplaza, en lugar de recalcular todo para cada subarreglo. Al avanzar, **sumas** el elemento que entra y **restas** el que sale.

## Cómo se usa

Hay dos variantes.

- **Ventana de tamaño fijo k**: calcula la primera ventana; luego, en cada paso, suma `a[i]` y resta `a[i - k]`.
- **Ventana de tamaño variable**: `right` avanza siempre y agrega elementos; mientras la ventana incumpla (o cumpla) una condición, `left` avanza y quita elementos. Aquí se guarda el mejor tamaño visto.

Cada índice entra y sale de la ventana una sola vez, por eso el total es lineal.

## Ejemplo

Mayor suma de 3 elementos consecutivos (tamaño fijo) y la ventana más corta con suma al menos 8 (variable).

```java
int[] a = {1, 2, 6, 2, 4, 1};
int k = 3;
int suma = 0;
for (int i = 0; i < k; i++) suma += a[i];
int mejor = suma;
for (int i = k; i < a.length; i++) {
    suma += a[i] - a[i - k];
    mejor = Math.max(mejor, suma);
}
System.out.println(mejor); // imprime: 12

int objetivo = 8, left = 0, total = 0, minimo = Integer.MAX_VALUE;
for (int right = 0; right < a.length; right++) {
    total += a[right];
    while (total >= objetivo) {
        minimo = Math.min(minimo, right - left + 1);
        total -= a[left++];
    }
}
System.out.println(minimo); // imprime: 2
```

## Complejidad

- Tiempo: O(n); aunque haya un `while` dentro del `for`, `left` y `right` solo avanzan.
- Espacio: O(1) con sumas; O(k) si guardas conteos en un `Map`.

## Errores comunes

- Recalcular la suma completa en cada paso (vuelve a O(n·k)).
- Tamaño de ventana equivocado: es `right - left + 1`, no `right - left`.
- Olvidar manejar `k` mayor que el arreglo.
- Usar la variante de tamaño variable con **números negativos**: la ventana deja de ser monótona y la técnica no funciona.
- No inicializar el mínimo con un valor "infinito" y no tratar el caso sin respuesta.

## En entrevistas

- Señales: "subarreglo o subcadena contigua", "longitud máxima o mínima", "suma de k elementos".
- Decide primero: ¿tamaño fijo o variable? ¿Qué condición mueve `left`?
- Se combina con `HashMap` o `HashSet` para subcadenas sin repetidos.
- Explica por qué es O(n) aunque haya dos ciclos anidados.
