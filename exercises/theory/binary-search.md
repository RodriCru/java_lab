## Qué es

La búsqueda binaria encuentra un valor en datos **ordenados** descartando la mitad del espacio en cada paso. En vez de revisar uno por uno (O(n)), tardas O(log n): para mil millones de elementos, unos 30 pasos.

## Cómo se usa

Mantén dos límites, `low` y `high`, que encierran donde puede estar la respuesta.

- Calcula `mid = low + (high - low) / 2` (evita desbordar el `int`).
- Si `numbers[mid]` es el objetivo, terminaste.
- Si es menor que el objetivo, descarta la izquierda: `low = mid + 1`.
- Si es mayor, descarta la derecha: `high = mid - 1`.
- Repite mientras `low <= high`. Si sales del ciclo, no está.

La misma idea sirve para variantes: buscar la **primera** o **última** aparición (sigue buscando al encontrar), o buscar en un arreglo **rotado** (en cada paso, una de las dos mitades siempre está ordenada: decide en cuál cae el objetivo).

## Ejemplo

```java
int[] numeros = {2, 5, 8, 12, 16, 23, 38};
int objetivo = 23;
int low = 0, high = numeros.length - 1, pos = -1;
while (low <= high) {
    int mid = low + (high - low) / 2;
    if (numeros[mid] == objetivo) {
        pos = mid;
        break;
    } else if (numeros[mid] < objetivo) {
        low = mid + 1;
    } else {
        high = mid - 1;
    }
}
System.out.println(pos); // imprime: 5
```

## Complejidad

- Tiempo: O(log n).
- Espacio: O(1) en la versión iterativa; O(log n) si es recursiva.
- Requisito: los datos deben estar ordenados (o tener una estructura monótona).

## Errores comunes

- Ciclo infinito por actualizar con `low = mid` o `high = mid` sin avanzar.
- Usar `<` en lugar de `<=` en la condición y perder el caso de un solo elemento.
- Calcular `(low + high) / 2` y desbordar con arreglos enormes.
- Aplicarla a datos sin ordenar.
- En duplicados, devolver cualquier posición cuando piden la primera.

## En entrevistas

- Cuando oigas "ordenado" y "mejor que O(n)", piensa en búsqueda binaria.
- Variantes habituales: primera o última aparición, punto de inserción, arreglo rotado.
- Pruébala a mano con arreglos de 0, 1 y 2 elementos antes de dar el código por bueno.
- Java ofrece `Arrays.binarySearch`: si no encuentra, devuelve un valor negativo.
