## Qué es

Una función (en Java, un **método**) agrupa instrucciones con un nombre, recibe parámetros y puede devolver un valor. Sirve para no repetir código y para dividir un problema en piezas pequeñas.

## Cómo se usa

- Firma: `modificador tipoRetorno nombre(tipo param, ...)`. Si no devuelve nada, el tipo es `void`.
- `static` significa que pertenece a la clase y se llama sin crear un objeto.
- **Sobrecarga**: varios métodos con el mismo nombre y distintos parámetros (tipo o cantidad). El tipo de retorno solo no basta para distinguirlos.
- **Varargs**: `int... numeros` acepta cero o más argumentos y dentro del método se usa como un arreglo. Debe ser el último parámetro.
- **Paso por valor**: Java siempre copia el argumento. Con objetos se copia la referencia, así que puedes modificar el objeto, pero no reasignar la variable del que llama.

## Ejemplo

```java
static int area(int lado) {
    return lado * lado;
}

static int area(int base, int altura) {
    return base * altura;
}

static int sumar(int... numeros) {
    int total = 0;
    for (int n : numeros) {
        total += n;
    }
    return total;
}

static void agregar(List<String> lista) {
    lista.add("nuevo"); // el que llama sí ve este cambio
    lista = new ArrayList<>(); // el que llama NO ve esta reasignación
}

static void probar() {
    System.out.println(area(3)); // imprime: 9
    System.out.println(area(2, 5)); // imprime: 10
    System.out.println(sumar()); // imprime: 0
    System.out.println(sumar(1, 2, 3)); // imprime: 6
}
```

## Errores comunes

- Olvidar el `return` en algún camino del método (no compila).
- Creer que Java pasa objetos por referencia: pasa una copia de la referencia.
- Modificar un parámetro primitivo esperando que cambie fuera: no cambia.
- Sobrecargas ambiguas, por ejemplo `f(int, long)` y `f(long, int)` llamadas con dos `int`.
- Llamar a un método de instancia desde uno `static` sin tener un objeto.

## En entrevistas

- ¿Java pasa por valor o por referencia? Siempre por valor.
- Diferencia entre sobrecarga (mismo nombre, distinta firma, se decide al compilar) y sobrescritura (en subclases).
- Cómo funcionan los varargs por dentro (un arreglo) y por qué solo puede haber uno.
- Qué significa que un método sea `static`.
