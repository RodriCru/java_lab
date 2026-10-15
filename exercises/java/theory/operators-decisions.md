## Qué es

Los operadores y las estructuras de decisión controlan qué camino sigue tu programa. Con ellos combinas condiciones (`&&`, `||`, `!`), comparas valores y eliges entre alternativas con `if`, el operador ternario o `switch`.

## Cómo se usa

- **Comparación**: `==`, `!=`, `<`, `>`, `<=`, `>=`. Devuelven `boolean`.
- **Lógicos**: `&&` (y), `||` (o), `!` (no). Hacen **cortocircuito**: si el resultado ya se conoce, no evalúan el lado derecho.
- **Aritméticos**: `+ - * / %`. Con enteros, `/` descarta los decimales y `%` da el resto.
- **Ternario**: `condicion ? valorSiSi : valorSiNo`.
- **Bit a bit**: `&`, `|`, `^`, `~`, `<<`, `>>`. Útiles para máscaras y para revisar si un número es par (`n & 1`).
- **switch moderno** (Java 14+): usa `->`, no necesita `break` y puede devolver un valor.

## Ejemplo

```java
int mes = 2;
int anio = 2024;
boolean bisiesto = (anio % 4 == 0 && anio % 100 != 0) || anio % 400 == 0;

int dias = switch (mes) {
    case 4, 6, 9, 11 -> 30;
    case 2 -> bisiesto ? 29 : 28;
    default -> 31;
};
System.out.println(dias); // imprime: 29

int n = 7;
String tipo = (n & 1) == 0 ? "par" : "impar";
System.out.println(tipo); // imprime: impar
```

## Errores comunes

- Usar `=` en lugar de `==` dentro de una condición.
- Comparar `String` con `==`: compara referencias, no contenido. Usa `equals`.
- Olvidar que `5 / 2` es `2` con enteros; usa `5 / 2.0` para decimales.
- En un `switch` clásico (con `:`) olvidar el `break` y caer al siguiente caso.
- Ordenar mal los `if / else if`: la primera condición verdadera gana, así que pon primero los casos más específicos.

## En entrevistas

- Diferencia entre `&&` y `&` con booleanos: el primero hace cortocircuito, el segundo siempre evalúa ambos lados.
- Cómo evitar un `NullPointerException` con cortocircuito: `x != null && x.length() > 0`.
- Qué imprime `-7 % 3` (da `-1`: el signo sigue al dividendo).
- Cuándo preferir `switch` sobre una cadena de `if` y qué tipos admite (enteros, `String`, `enum`).
