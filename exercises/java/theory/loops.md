## Qué es

Los ciclos repiten un bloque de código mientras se cumpla una condición o para cada elemento de una colección. Java ofrece `for`, `while`, `do-while` y el `for` mejorado (foreach).

## Cómo se usa

- `for (inicio; condicion; paso)`: cuando sabes cuántas vueltas das o necesitas el índice.
- `while (condicion)`: cuando no sabes cuántas vueltas habrá. Evalúa la condición **antes** de cada vuelta.
- `do { ... } while (condicion);`: ejecuta al menos una vez.
- `for (Tipo x : coleccion)`: recorre arreglos y colecciones sin manejar índices.
- `break` sale del ciclo; `continue` salta a la siguiente vuelta.
- Ciclos anidados: el interno completa todas sus vueltas por cada vuelta del externo. Sirven para matrices y patrones de texto.

## Ejemplo

```java
int[] datos = {4, 8, 15, 16};

int suma = 0;
for (int i = 0; i < datos.length; i++) {
    suma += datos[i];
}
System.out.println(suma); // imprime: 43

int pares = 0;
for (int d : datos) {
    if (d % 2 != 0) continue;
    pares++;
}
System.out.println(pares); // imprime: 3

StringBuilder fila = new StringBuilder();
for (int i = 1; i <= 3; i++) {
    for (int j = 0; j < i; j++) {
        fila.append('*');
    }
    fila.append('\n');
}
System.out.print(fila);
// imprime: * (línea 1), ** (línea 2), *** (línea 3)
```

## Errores comunes

- Error de uno de más o de menos (off-by-one): usar `<=` con `length` provoca `ArrayIndexOutOfBoundsException`.
- Ciclo infinito por olvidar actualizar la variable de la condición.
- Modificar una colección dentro de un foreach: lanza `ConcurrentModificationException`.
- Declarar acumuladores dentro del ciclo, de modo que se reinician en cada vuelta.
- Poner `;` justo después del `for (...)` o del `while (...)`, lo que deja el cuerpo vacío.

## En entrevistas

- Cuándo elegir `for` clásico y cuándo foreach (el foreach no da el índice ni permite modificar la colección).
- Cómo salir de dos ciclos anidados a la vez (etiquetas con `break etiqueta;` o extraer un método con `return`).
- Complejidad de un ciclo doble: normalmente O(n²).
- Diferencia entre `while` y `do-while`.
