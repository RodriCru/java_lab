## Qué es

Las estructuras de datos de `java.util` te dan acceso rápido a la información. Para entrevistas, cuatro cubren casi todo: `Map` (clave a valor), `Set` (sin repetidos), `Deque` o `Stack` (pila) y `Queue` (cola).

La idea central: **cambiar memoria por tiempo**. Guardar lo ya visto en un `HashMap` o `HashSet` convierte una búsqueda de O(n) en O(1) promedio y elimina ciclos anidados.

## Cómo se usa

- `HashMap<K,V>`: `put`, `get`, `containsKey`, `getOrDefault`, `merge`. No garantiza orden.
- `LinkedHashMap`: igual, pero conserva el orden de inserción. `TreeMap` mantiene las claves ordenadas (O(log n)).
- `HashSet<T>`: `add` devuelve `false` si el elemento ya estaba. `contains` es O(1).
- `ArrayDeque<T>`: úsalo como pila (`push`, `pop`, `peek`) o como cola (`offer`, `poll`, `peek`). Es preferible a `Stack`.
- `PriorityQueue<T>`: cola donde `poll` entrega el menor elemento (montículo binario).

## Ejemplo

Contar cuántas veces aparece cada letra y detectar si una pila de números está en orden.

```java
Map<Character, Integer> conteo = new HashMap<>();
for (char c : "banana".toCharArray()) {
    conteo.merge(c, 1, Integer::sum);
}
System.out.println(conteo.get('a')); // imprime: 3

Deque<Integer> pila = new ArrayDeque<>();
pila.push(1);
pila.push(2);
System.out.println(pila.pop()); // imprime: 2

Set<Integer> vistos = new HashSet<>();
System.out.println(vistos.add(7)); // imprime: true
System.out.println(vistos.add(7)); // imprime: false
```

## Complejidad

- `HashMap` y `HashSet`: `put`, `get`, `contains` en O(1) promedio; recorrerlos es O(n).
- `TreeMap` y `TreeSet`: O(log n) por operación.
- `ArrayDeque`: `push`, `pop`, `offer`, `poll` en O(1).
- `PriorityQueue`: `offer` y `poll` en O(log n); `peek` en O(1).
- Espacio: O(n) por lo que guardes.

## Errores comunes

- Usar objetos propios como clave sin implementar `equals` y `hashCode`.
- Modificar un `Map` mientras lo recorres con for-each: lanza `ConcurrentModificationException`.
- Llamar `pop` o `peek` sobre una pila vacía: `pop` lanza excepción y `peek` devuelve `null`. Revisa `isEmpty()`.
- Comparar `Integer` con `==` en lugar de `equals` (falla fuera del rango -128 a 127).
- Usar `get` y recibir `null` al desempaquetar a `int`: usa `getOrDefault`.

## En entrevistas

- Si piensas "¿ya vi este valor?", usa un `Set`. Si piensas "¿cuántas veces?" o "¿en qué posición?", usa un `Map`.
- Problemas de paréntesis, deshacer o expresiones anidadas piden una **pila**.
- Pregunta típica: diferencia entre `HashMap`, `LinkedHashMap` y `TreeMap`.
- Menciona siempre el costo en espacio extra: O(n) a cambio de bajar el tiempo de O(n²) a O(n).
