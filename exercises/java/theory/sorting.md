## Qué es

Ordenar es colocar los elementos según un criterio. En entrevistas se piden los algoritmos clásicos para ver si entiendes la idea, aunque en código real uses `Arrays.sort` o `Collections.sort`.

## Cómo se usa

- **Burbuja**: compara vecinos y los intercambia si están al revés; repite hasta que no haya cambios. Simple pero lento.
- **Selección**: en cada pasada busca el mínimo del resto y lo coloca en su lugar.
- **Mezcla (merge sort)**: divide el arreglo a la mitad, ordena cada mitad y las **mezcla** comparando sus frentes. Divide y vencerás.
- **Rápido (quick sort)**: elige un **pivote**, deja a la izquierda los menores y a la derecha los mayores, y repite en cada lado.
- **Selección del k-ésimo**: no hace falta ordenar todo; basta ordenar y tomar una posición, o mantener un `PriorityQueue` de tamaño k.

## Ejemplo

Mezcla de dos listas ya ordenadas, el paso clave de merge sort.

```java
int[] a = {1, 4, 9};
int[] b = {2, 3, 10};
int[] r = new int[a.length + b.length];
int i = 0, j = 0, k = 0;
while (i < a.length && j < b.length) {
    r[k++] = a[i] <= b[j] ? a[i++] : b[j++];
}
while (i < a.length) r[k++] = a[i++];
while (j < b.length) r[k++] = b[j++];
System.out.println(Arrays.toString(r)); // imprime: [1, 2, 3, 4, 9, 10]

int[] x = {5, 2, 8, 1};
Arrays.sort(x);
System.out.println(Arrays.toString(x)); // imprime: [1, 2, 5, 8]
```

## Complejidad

- Burbuja y selección: O(n²) tiempo, O(1) espacio.
- Merge sort: O(n log n) siempre, O(n) espacio extra. Es **estable**.
- Quick sort: O(n log n) promedio, O(n²) en el peor caso (pivote malo); O(log n) de pila.
- `Arrays.sort` en primitivos usa quick sort de doble pivote; en objetos usa TimSort (estable).
- k-ésimo con `PriorityQueue` de tamaño k: O(n log k).

## Errores comunes

- Errores de un índice (off-by-one) al mezclar o al particionar.
- Olvidar copiar los elementos que sobran de una mitad en la mezcla.
- Pivote mal elegido, o particiones que no avanzan y causan recursión infinita.
- Pensar que "ordenado" y "estable" son lo mismo: estable significa que iguales conservan su orden relativo.

## En entrevistas

- Te piden explicar la complejidad y cuándo usar cada algoritmo.
- Pregunta frecuente: ¿qué es un ordenamiento estable y cuál de los clásicos lo es?
- Si piden "el k-ésimo mayor", piensa en ordenar o en un heap antes de escribir ciclos.
- Para un arreglo casi ordenado o pequeño, algoritmos simples pueden bastar.
