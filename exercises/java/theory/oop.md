## Qué es

Este tema reúne las piezas de Java que hacen que tus objetos se comparen, ordenen y guarden bien: `equals` y `hashCode`, `Comparable`, `Comparator`, `record` e inmutabilidad.

## Cómo se usa

- **`equals`**: define cuándo dos objetos son "iguales" por contenido. Por defecto compara referencias (como `==`).
- **`hashCode`**: debe ser coherente con `equals`; si dos objetos son iguales, tienen el mismo hash. Es lo que usan `HashMap` y `HashSet`.
- **`Comparable<T>`**: la clase define su orden natural con `compareTo`. Devuelve negativo, cero o positivo.
- **`Comparator<T>`**: un orden externo. Se arma con `Comparator.comparing(...)`, `thenComparing(...)` y `reversed()`.
- **`record`**: clase de datos inmutable; genera constructor, accesores, `equals`, `hashCode` y `toString`.
- **Inmutabilidad**: campos `final`, sin setters, y copias defensivas si guardas listas o arreglos.

## Ejemplo

```java
record Punto(int x, int y) {}

class Libro implements Comparable<Libro> {
    final String titulo;
    final int paginas;
    Libro(String titulo, int paginas) { this.titulo = titulo; this.paginas = paginas; }
    public int compareTo(Libro o) { return Integer.compare(paginas, o.paginas); }
    public String toString() { return titulo; }
}

Set<Punto> puntos = new HashSet<>();
puntos.add(new Punto(1, 2));
System.out.println(puntos.contains(new Punto(1, 2))); // imprime: true

List<Libro> libros = new ArrayList<>(List.of(new Libro("B", 300), new Libro("A", 100)));
Collections.sort(libros);
System.out.println(libros); // imprime: [A, B]
libros.sort(Comparator.comparing((Libro l) -> l.titulo).reversed());
System.out.println(libros); // imprime: [B, A]
```

## Errores comunes

- Sobrescribir `equals` sin `hashCode`: el objeto "desaparece" dentro de un `HashSet`.
- Firmar mal `equals(Object o)` (por ejemplo `equals(Libro o)`), que sobrecarga en vez de sobrescribir.
- Restar enteros en `compareTo` (`a - b`): puede desbordar. Usa `Integer.compare`.
- No manejar `null` o clases distintas en `equals`.
- Creer que un `record` con una lista adentro es inmutable: la lista sigue siendo modificable.

## En entrevistas

- Pregunta clásica: el contrato entre `equals` y `hashCode`.
- Diferencia entre `Comparable` (orden natural, uno solo) y `Comparator` (varios criterios externos).
- Cómo hacer una clase inmutable: `final`, sin setters, copias defensivas.
- Cuándo usar un `record`: objetos de datos pequeños y sin comportamiento complejo.
