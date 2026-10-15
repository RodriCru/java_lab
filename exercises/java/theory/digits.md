## Qué es

Trabajar con los **dígitos** de un número entero sin convertirlo a texto, usando solo división y módulo entre 10.

## Cómo se usa

- `n % 10` da el **último dígito**: `472 % 10` es `2`.
- `n / 10` **quita el último dígito**: `472 / 10` es `47`.
- Se combinan en un ciclo que termina cuando `n` llega a `0`.
- Para **sumar** dígitos, acumula `n % 10` en cada vuelta.
- Para **invertir**, arma un nuevo número: `invertido = invertido * 10 + digito`.
- Para **contar** dígitos, incrementa un contador por vuelta.
- Trabaja con una copia de `n` si necesitas el valor original después del ciclo.
- Con números negativos, usa `Math.abs(n)` antes de empezar.

## Ejemplo

```java
int n = 1234;
int copia = n;
int suma = 0;
int invertido = 0;
int cantidad = 0;
while (copia > 0) {
    int digito = copia % 10;
    suma += digito;
    invertido = invertido * 10 + digito;
    cantidad++;
    copia /= 10;
}
System.out.println(suma + " " + invertido + " " + cantidad);
// imprime: 10 4321 4

// Elevar cada dígito a una potencia con Math.pow
System.out.println((int) Math.pow(3, 3));
// imprime: 27
```

## Errores comunes

- Modificar `n` dentro del ciclo y luego necesitarlo para comparar.
- Usar `while (n > 0)` con el valor `0` de entrada, que no entra al ciclo (el `0` tiene un dígito).
- Que el invertido desborde un `int` si el número es muy grande.
- Perder los ceros finales al invertir: `120` invertido es `21`.

## En entrevistas

- Suma de dígitos, inversión y verificación de palíndromo numérico sin usar `String`.
- Números de Armstrong: cada dígito elevado a la cantidad de dígitos, sumados, dan el número original.
- Comparar la solución con la de convertir a `String` y explicar cuándo conviene cada una.
