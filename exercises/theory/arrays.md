## Qué es

Un **arreglo** es una colección de tamaño fijo de elementos del mismo tipo, accesibles por índice. El primer índice es `0` y el último es `length - 1`.

## Cómo se usa

- Crear vacío: `int[] a = new int[5];` (se llena con ceros).
- Crear con valores: `int[] a = {4, 8, 1};`.
- Leer y escribir: `a[0]`, `a[2] = 7;`.
- Tamaño: `a.length` (sin paréntesis).
- Recorrer: `for (int i = 0; i < a.length; i++)` o `for (int x : a)`.
- Utilidades de `Arrays`: `Arrays.toString(a)`, `Arrays.sort(a)`, `Arrays.copyOf(a, n)`, `Arrays.equals(a, b)`, `Arrays.fill(a, v)`.
- Patrones típicos: recorrer guardando el mayor visto, contar coincidencias, intercambiar con dos índices (`i` desde el inicio, `j` desde el final).
- Los arreglos son **referencias**: `b = a` no copia, ambos apuntan al mismo arreglo.

## Ejemplo

```java
int[] a = {5, 2, 9, 2, 7};
int mayor = a[0];
int veces = 0;
for (int x : a) {
    if (x > mayor) mayor = x;
    if (x == 2) veces++;
}
System.out.println(mayor + " " + veces);
// imprime: 9 2

int i = 0, j = a.length - 1;
while (i < j) {
    int tmp = a[i];
    a[i] = a[j];
    a[j] = tmp;
    i++;
    j--;
}
System.out.println(Arrays.toString(a));
// imprime: [7, 2, 9, 2, 5]
```

## Errores comunes

- `ArrayIndexOutOfBoundsException` por usar `i <= a.length`.
- Imprimir el arreglo directamente: sale una dirección. Usa `Arrays.toString`.
- Comparar arreglos con `==` o `.equals`: usa `Arrays.equals`.
- Iniciar el "mayor" en `0` y fallar con arreglos de negativos; empieza con `a[0]`.
- Olvidar que el arreglo vacío no tiene `a[0]`.

## En entrevistas

- Encontrar máximo, contar ocurrencias, revisar si está ordenado.
- Invertir en el lugar con dos índices, sin arreglo extra.
- Eliminar duplicados y explicar el costo de cada enfoque (anidado O(n²), con `Set` O(n)).
- Diferencia entre arreglo y `ArrayList`.
