## Qué es

La programación dinámica (DP) resuelve un problema dividiéndolo en subproblemas **que se repiten** y guardando cada resultado para calcularlo una sola vez. Se puede aplicar cuando hay **subestructura óptima** (la respuesta se arma con respuestas de subproblemas) y **subproblemas superpuestos**.

## Cómo se usa

Sigue estos pasos.

- Define qué significa `dp[i]` con una frase clara (por ejemplo, "formas de llegar al paso i").
- Escribe la **recurrencia**: cómo `dp[i]` depende de valores anteriores.
- Define los **casos base** (`dp[0]`, a veces `dp[1]`).
- Llena la tabla de menor a mayor (**bottom-up**), o usa recursión con un `Map` o arreglo de memoria (**top-down** o memoización).
- La respuesta suele ser `dp[n]`.

Si `dp[i]` solo depende de los últimos uno o dos valores, puedes guardar solo esas variables y ahorrar espacio.

## Ejemplo

Número de caminos para llegar a una celda avanzando solo a la derecha o abajo en una cuadrícula de 3x3.

```java
int n = 3;
int[][] dp = new int[n][n];
for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
        if (i == 0 || j == 0) {
            dp[i][j] = 1;
        } else {
            dp[i][j] = dp[i - 1][j] + dp[i][j - 1];
        }
    }
}
System.out.println(dp[n - 1][n - 1]); // imprime: 6
```

## Complejidad

- Tiempo: número de estados multiplicado por el costo de cada transición. Aquí, O(n²).
- Espacio: el tamaño de la tabla, a menudo reducible (una fila o dos variables).
- La recursión sin memoria puede ser exponencial; con memoria baja a polinomial.

## Errores comunes

- No definir con precisión qué representa `dp[i]`.
- Casos base incorrectos o faltantes (por ejemplo, el valor para `0`).
- Usar `Integer.MAX_VALUE` como "imposible" y luego sumarle 1: desborda. Usa un valor grande seguro o comprueba antes.
- Llenar la tabla en un orden en que aún no existen los datos que necesitas.
- Confundir "mínimo" (se inicializa con infinito) con "contar formas" (se inicializa con 0 o 1).

## En entrevistas

- Señales: "cuántas formas", "mínimo o máximo número de...", "¿es posible alcanzar...?".
- Empieza con la recursión simple, detecta los cálculos repetidos y añade memoria.
- Verbaliza la recurrencia y los casos base antes de escribir código.
- Menciona la optimización de espacio cuando solo hace falta la fila o el valor anterior.
