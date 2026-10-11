## Qué es

Desde Java 8, el lenguaje permite un estilo más declarativo: describes **qué** quieres y no cómo iterarlo. Las piezas son las lambdas, las referencias a métodos, `Optional` y los streams.

## Cómo se usa

- **Lambda**: función anónima, `(a, b) -> a + b`. Se asigna a interfaces funcionales como `Function`, `Predicate`, `Consumer` o `Supplier`.
- **Referencia a método**: atajo, como `String::toUpperCase` o `Integer::sum`.
- **Optional**: contenedor que puede tener o no un valor. Usa `Optional.of`, `Optional.empty`, `map`, `orElse` e `isPresent`. Evita devolver `null`.
- **Streams**: `lista.stream()` seguido de operaciones intermedias (`filter`, `map`, `sorted`, `distinct`) y una terminal (`collect`, `toList`, `sum`, `count`, `findFirst`, `max`, `reduce`).
- `Collectors.joining(", ")` une textos; `Collectors.groupingBy` agrupa.

Un stream no modifica la fuente y solo se consume una vez.

## Ejemplo

```java
List<Integer> nums = List.of(1, 2, 3, 4, 5, 6);
List<Integer> cuadrados = nums.stream()
    .filter(n -> n % 2 == 0)
    .map(n -> n * n)
    .collect(Collectors.toList());
System.out.println(cuadrados); // imprime: [4, 16, 36]

String texto = Stream.of("uno", "dos", "tres")
    .map(String::toUpperCase)
    .collect(Collectors.joining("-"));
System.out.println(texto); // imprime: UNO-DOS-TRES

Optional<String> larga = Stream.of("a", "abc", "ab")
    .filter(s -> s.length() > 2)
    .findFirst();
System.out.println(larga.orElse("ninguna")); // imprime: abc

int suma = nums.stream().mapToInt(Integer::intValue).sum();
System.out.println(suma); // imprime: 21
```

## Errores comunes

- Olvidar la operación terminal: sin ella, el stream no hace nada.
- Reutilizar un stream ya consumido (`IllegalStateException`).
- Llamar `Optional.get()` sin comprobar `isPresent()`; mejor `orElse` o `orElseThrow`.
- Usar `Optional.of(null)`: lanza excepción. Para valores posiblemente nulos, `Optional.ofNullable`.
- Modificar variables externas dentro de una lambda: solo puedes usar variables **efectivamente finales**.
- Los elementos `null` dentro del stream provocan `NullPointerException` en `map` o `filter`: fíltralos antes.

## En entrevistas

- Diferencia entre operaciones intermedias (perezosas) y terminales.
- Cuándo un stream es peor que un ciclo: lógica con estado, índices o corte temprano complejo.
- Por qué `Optional` mejora una API frente a devolver `null`.
- Saber convertir un ciclo con `if` y acumulador a `filter` más `map` más `collect`.
