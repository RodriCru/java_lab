## Qué es

En muchos problemas los datos llegan como texto: una línea como `"3 8 15"` o `"a=1,b=2"`. Parsear es convertir ese texto en números, pares o matrices con los que sí puedes calcular.

## Cómo se usa

- `texto.trim()` quita espacios al inicio y al final.
- `texto.split(",")` parte por un separador y devuelve un `String[]`. Usa `split("\\s+")` para separar por uno o más espacios.
- `Integer.parseInt(s)`, `Long.parseLong(s)` y `Double.parseDouble(s)` convierten texto en número.
- Si el texto no es un número válido, lanzan `NumberFormatException`.
- `split(sep, 2)` limita el número de partes: útil para separar `clave=valor` cuando el valor puede contener el separador.
- Una matriz en texto suele venir por filas: separa primero por línea o por `;` y luego cada fila por espacios.
- `Scanner` sobre un `String` también sirve: `new Scanner(texto)` con `nextInt()`.

## Ejemplo

```java
String linea = "  10 20  30 ";
String[] partes = linea.trim().split("\\s+");
int suma = 0;
for (String p : partes) {
    suma += Integer.parseInt(p);
}
System.out.println(suma); // imprime: 60

String par = "color=rojo=oscuro";
String[] kv = par.split("=", 2);
System.out.println(kv[0] + " -> " + kv[1]); // imprime: color -> rojo=oscuro

String m = "1 2;3 4";
String[] filas = m.split(";");
int[][] matriz = new int[filas.length][];
for (int i = 0; i < filas.length; i++) {
    String[] celdas = filas[i].trim().split(" ");
    matriz[i] = new int[celdas.length];
    for (int j = 0; j < celdas.length; j++) {
        matriz[i][j] = Integer.parseInt(celdas[j]);
    }
}
System.out.println(matriz[1][0]); // imprime: 3
```

## Errores comunes

- Hacer `split(" ")` con varios espacios seguidos: produce elementos vacíos que fallan en `parseInt`.
- Olvidar `trim()` y recibir `NumberFormatException` por un espacio sobrante.
- Usar `split("|")` o `split(".")`: son expresiones regulares; hay que escapar con `"\\|"` y `"\\."`.
- Asumir que `split` conserva las cadenas vacías del final: las descarta.
- No validar el caso de línea vacía antes de acceder a `partes[0]`.

## En entrevistas

- Qué hacer con entradas inválidas: capturar `NumberFormatException` o validar antes.
- Diferencia entre `Integer.parseInt` (devuelve `int`) y `Integer.valueOf` (devuelve `Integer`).
- Desbordamiento: un número grande puede requerir `long` o `BigInteger`.
