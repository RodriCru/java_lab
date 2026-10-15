## Qué es

Una **matriz** es un arreglo de arreglos (`int[][]`): filas y columnas. Se accede con `m[fila][columna]`. Los problemas de patrones y matrices se resuelven casi siempre con **dos ciclos anidados**.

## Cómo se usa

- Crear: `int[][] m = new int[3][4];` (3 filas, 4 columnas), o `int[][] m = {{1, 2}, {3, 4}};`.
- Filas: `m.length`. Columnas de la fila `i`: `m[i].length`.
- Recorrido completo: ciclo externo por fila `i`, ciclo interno por columna `j`.
- Diagonal principal: celdas donde `i == j`. Diagonal secundaria en una matriz cuadrada de lado `n`: `i + j == n - 1`.
- **Transpuesta**: el valor de `[i][j]` pasa a `[j][i]`; si la matriz no es cuadrada, el resultado tiene las dimensiones invertidas.
- Patrones de texto (triángulos, pirámides): el ciclo externo es la fila y el interno imprime espacios o símbolos según `i`.
- Imprimir una matriz: `Arrays.deepToString(m)`.

## Ejemplo

```java
int[][] m = {
    {1, 2, 3},
    {4, 5, 6}
};

int suma = 0;
for (int i = 0; i < m.length; i++) {
    for (int j = 0; j < m[i].length; j++) {
        suma += m[i][j];
    }
}
System.out.println(suma);
// imprime: 21

int[][] t = new int[m[0].length][m.length];
for (int i = 0; i < m.length; i++) {
    for (int j = 0; j < m[i].length; j++) {
        t[j][i] = m[i][j];
    }
}
System.out.println(Arrays.deepToString(t));
// imprime: [[1, 4], [2, 5], [3, 6]]

for (int i = 1; i <= 3; i++) {
    System.out.println("*".repeat(i));
}
// imprime: *
// imprime: **
// imprime: ***
```

## Errores comunes

- Confundir filas y columnas al crear la matriz o al indexar.
- Usar `m.length` para ambos límites en matrices no cuadradas.
- Usar `Arrays.toString` en una matriz: sale basura; es `deepToString`.
- Copiar con `m.clone()`: es superficial, las filas se comparten.

## En entrevistas

- Suma, diagonales y transpuesta de una matriz.
- Rotar una matriz 90 grados.
- Recorridos sobre cuadrículas: moverse a los cuatro vecinos cuidando los límites antes de acceder.
