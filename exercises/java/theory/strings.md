## Qué es

`String` es una secuencia de caracteres **inmutable**: ninguna operación modifica el original, todas devuelven una cadena nueva. Para construir texto por partes se usa `StringBuilder`, que sí es mutable y eficiente.

## Cómo se usa

- `s.length()`, `s.charAt(i)`, `s.substring(ini, finExclusivo)`, `s.indexOf("x")`.
- `s.equals(otra)` compara contenido; `equalsIgnoreCase` ignora mayúsculas; `compareTo` ordena alfabéticamente.
- `s.toLowerCase()`, `s.toUpperCase()`, `s.trim()`, `s.replace("a", "b")`, `s.contains("x")`.
- `s.toCharArray()` da un `char[]` para recorrer o reordenar; `new String(arreglo)` lo vuelve texto.
- `Character.isLetter(c)`, `Character.isDigit(c)`, `Character.toUpperCase(c)`.
- Los `char` son números: `c - 'a'` da la posición de la letra (0 a 25), muy útil para contar frecuencias.
- `StringBuilder`: `append`, `reverse`, `insert`, `setCharAt`, `toString`.

## Ejemplo

```java
String texto = "Hola Mundo";

StringBuilder sb = new StringBuilder();
for (int i = texto.length() - 1; i >= 0; i--) {
    sb.append(texto.charAt(i));
}
System.out.println(sb); // imprime: odnuM aloH

int[] frecuencia = new int[26];
for (char c : "banana".toCharArray()) {
    frecuencia[c - 'a']++;
}
System.out.println(frecuencia[0]); // imprime: 3

String a = new String("java");
String b = "java";
System.out.println(a == b); // imprime: false
System.out.println(a.equals(b)); // imprime: true
```

## Errores comunes

- Comparar con `==` en lugar de `equals`.
- Olvidar que `substring` excluye el índice final.
- Llamar `s.toUpperCase()` sin asignar el resultado: `s` no cambia.
- Concatenar con `+` dentro de un ciclo grande: crea muchas cadenas; usa `StringBuilder`.
- Acceder a `charAt(length())`: lanza `StringIndexOutOfBoundsException`.

## En entrevistas

- Por qué `String` es inmutable (seguridad, caché de hash, el pool de cadenas).
- Diferencia entre `String`, `StringBuilder` y `StringBuffer` (este último es sincronizado).
- Cómo verificar un palíndromo o un anagrama (dos punteros, o arreglo de frecuencias).
- Complejidad de invertir una cadena: O(n) en tiempo y espacio.
