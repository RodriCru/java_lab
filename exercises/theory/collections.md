## Qué es

El framework de colecciones ofrece estructuras listas para guardar grupos de datos. Elegir la correcta es la mitad del problema.

## Cómo se usa

- **List** (`ArrayList`): ordenada, permite repetidos, acceso por índice con `get(i)`.
- **Set** (`HashSet`, `TreeSet`, `LinkedHashSet`): sin repetidos. `HashSet` es el más rápido y no garantiza orden; `TreeSet` ordena; `LinkedHashSet` conserva el orden de inserción.
- **Map** (`HashMap`, `TreeMap`): pares clave-valor con `put`, `get`, `getOrDefault`, `containsKey`, `merge`.
- **Queue / Deque** (`ArrayDeque`, `LinkedList`): `offer`/`poll`/`peek` para colas (FIFO); `push`/`pop` para pila (LIFO).
- Recorrido: foreach sobre la colección, o sobre `map.entrySet()`.
- Para borrar mientras recorres, usa un `Iterator` y `it.remove()`, o `removeIf`.
- Los genéricos (`List<Integer>`) fijan el tipo; no admiten primitivos, se usan `Integer`, `Double`...

## Ejemplo

```java
List<Integer> numeros = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
numeros.removeIf(n -> n % 2 == 0);
System.out.println(numeros); // imprime: [1, 3, 5]

Map<Character, Integer> conteo = new HashMap<>();
for (char c : "hola".toCharArray()) {
    conteo.put(c, conteo.getOrDefault(c, 0) + 1);
}
System.out.println(conteo.get('o')); // imprime: 1

Set<Integer> vistos = new HashSet<>();
System.out.println(vistos.add(7)); // imprime: true
System.out.println(vistos.add(7)); // imprime: false

Deque<String> pila = new ArrayDeque<>();
pila.push("a");
pila.push("b");
System.out.println(pila.pop()); // imprime: b
```

## Errores comunes

- Quitar elementos dentro de un foreach: `ConcurrentModificationException`.
- En `List<Integer>`, `remove(1)` borra por índice, no el valor 1; usa `remove(Integer.valueOf(1))`.
- Usar como clave de `HashMap` objetos sin `equals` y `hashCode` bien definidos.
- Esperar un orden de recorrido en `HashMap` o `HashSet`.
- Usar `Stack` o `LinkedList` por costumbre; `ArrayDeque` es la opción habitual.

## En entrevistas

- Diferencia entre `ArrayList` y `LinkedList` (acceso O(1) contra inserciones en los extremos).
- Complejidad de `HashMap`: O(1) promedio para `get` y `put`.
- Cuándo usar un `Set` para detectar duplicados en O(n).
- Cómo funciona un `HashMap` por dentro (buckets, hashCode, equals).
