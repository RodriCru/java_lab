## Qué es
Un programa en Java es una clase con métodos. Un **método** recibe datos (parámetros), hace algo y puede devolver un resultado con `return`. En estos ejercicios solo escribes el método: el laboratorio lo llama por ti con distintos datos y revisa lo que devuelve o imprime. Fíjate bien en el enunciado: si dice «devuelve», el test mira el valor de `return`; si dice «imprime», mira lo que sale en consola.

## Imprimir en pantalla
- `System.out.println(x)` imprime `x` y salta de línea.
- `System.out.print(x)` imprime sin saltar de línea.
- El operador `+` une texto con otros valores.

```java
String nombre = "Ana";
int edad = 30;
System.out.println("Hola, " + nombre);
System.out.println("Tienes " + edad + " años");
// imprime: Hola, Ana
// imprime: Tienes 30 años
```

## Imprimir no es devolver
Son dos cosas distintas. **Imprimir** escribe en la consola para que una persona lo lea; el programa no recibe nada de vuelta. **Devolver** (`return`) entrega un valor a quien llamó al método, que puede compararlo, guardarlo o usarlo.

```java
static String saludoImpreso(String nombre) {
    System.out.println("Hola, " + nombre);
    return "";
}

static String saludoDevuelto(String nombre) {
    return "Hola, " + nombre;
}
// saludoImpreso("Ana") escribe "Hola, Ana" en consola pero devuelve ""
// saludoDevuelto("Ana") no escribe nada pero devuelve "Hola, Ana"
```

Si el ejercicio pide devolver un `String`, imprimirlo no cuenta: el test solo ve lo que sale de `return`.

## Variables y operadores
Una variable guarda un valor y tiene un tipo: `int` (enteros), `double` (decimales), `boolean` (true o false) y `String` (texto). Operadores comunes: `+ - * /` y `%` (resto).

```java
int a = 7;
int b = 2;
System.out.println(a / b);
System.out.println(a % b);
System.out.println(7.0 / 2);
// imprime: 3
// imprime: 1
// imprime: 3.5
```

Cuidado: la división entre enteros **descarta los decimales**. Si quieres decimales, que al menos uno de los dos números sea `double`.

## Condicionales
`if` ejecuta un bloque solo si la condición es verdadera. Se comparan valores con `==`, `!=`, `<`, `<=`, `>`, `>=` y se combinan con `&&` (y) y `||` (o).

```java
int n = -3;
if (n > 0) {
    System.out.println("positivo");
} else if (n < 0) {
    System.out.println("negativo");
} else {
    System.out.println("cero");
}
// imprime: negativo
```

## Ciclos
`for` repite un bloque un número conocido de veces. Tiene tres partes: inicio, condición para seguir y paso.

```java
int suma = 0;
for (int i = 1; i <= 3; i++) {
    suma += i;
}
System.out.println(suma);
// imprime: 6
```

## Armar un texto dentro de un ciclo
Para construir un resultado repitiendo algo, crea una variable fuera del ciclo, ve agregándole texto en cada vuelta y devuélvela al final. La forma sencilla es concatenar con `+=`:

```java
String resultado = "";
for (int i = 0; i < 3; i++) {
    resultado += "ab";
}
System.out.println(resultado);
// imprime: ababab
```

Cuando el ciclo da muchas vueltas, `StringBuilder` es mejor: `append` agrega texto sin crear un `String` nuevo en cada vuelta, y `toString()` lo convierte en `String` al terminar.

```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 3; i++) {
    sb.append("ab");
}
String resultado = sb.toString();
System.out.println(resultado);
// imprime: ababab
```

La variable (`resultado` o `sb`) debe declararse **antes** del `for`: si la declaras dentro, se reinicia en cada vuelta y se pierde al salir del ciclo.

## Errores comunes
- Usar `=` (asignar) en lugar de `==` (comparar).
- Empezar o terminar el ciclo una vuelta antes o después (`<` contra `<=`).
- Olvidar `return` en un método que debe devolver algo.
- Imprimir el resultado en lugar de devolverlo, o devolver solo el último pedazo en vez del texto completo.
- Comparar textos con `==`: usa `texto.equals(otro)`.
