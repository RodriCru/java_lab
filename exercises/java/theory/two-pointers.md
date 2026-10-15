## Qué es

La técnica de dos punteros usa dos índices que recorren el arreglo para evitar un ciclo dentro de otro. Convierte soluciones O(n²) en O(n) aprovechando alguna propiedad: que el arreglo esté ordenado, o que debas reorganizarlo en el sitio.

## Cómo se usa

Hay dos patrones principales.

- **Extremos opuestos**: `left = 0` y `right = n - 1`, que se acercan. Sirve en arreglos ordenados: si la suma es muy grande, baja `right`; si es muy pequeña, sube `left`.
- **Lector y escritor** (rápido y lento): un puntero recorre todo y otro marca dónde escribir el siguiente elemento válido. Sirve para filtrar o compactar **en el mismo arreglo**, sin crear otro.

En ambos casos cada puntero avanza como máximo n veces, por eso el total es lineal.

## Ejemplo

Invertir un arreglo en el sitio, con punteros en los extremos, y compactar los pares.

```java
int[] a = {1, 2, 3, 4, 5};
int left = 0, right = a.length - 1;
while (left < right) {
    int tmp = a[left];
    a[left] = a[right];
    a[right] = tmp;
    left++;
    right--;
}
System.out.println(Arrays.toString(a)); // imprime: [5, 4, 3, 2, 1]

int[] b = {1, 2, 3, 4, 6};
int write = 0;
for (int read = 0; read < b.length; read++) {
    if (b[read] % 2 == 0) {
        b[write++] = b[read];
    }
}
System.out.println(write); // imprime: 3
```

## Complejidad

- Tiempo: O(n), una sola pasada.
- Espacio: O(1), porque trabajas en el sitio.
- Si necesitas ordenar primero, el costo total pasa a O(n log n).

## Errores comunes

- Usar `left <= right` cuando debería ser `left < right` (o al revés) y tocar el mismo elemento dos veces.
- Mover el puntero equivocado y quedar en un ciclo infinito.
- En el patrón lector-escritor, sobrescribir un valor que aún no leíste.
- Olvidar que el arreglo debe estar ordenado para el patrón de extremos.
- No manejar arreglos vacíos o de un solo elemento.

## En entrevistas

- Señales: "arreglo ordenado", "en el mismo arreglo", "sin memoria extra", "par que sume X".
- Siempre aclara si puedes modificar la entrada y si importa conservar el orden relativo.
- Compara con la fuerza bruta (O(n²)) y explica por qué los punteros nunca retroceden.
- Pregunta típica: palíndromos, suma de pares, eliminar duplicados, mover elementos.
