## Qué es

La programación orientada a objetos modela el problema con **clases** (plantillas) y **objetos** (instancias). Sus pilares básicos son encapsulamiento, herencia y polimorfismo.

## Cómo se usa

- **Encapsulamiento**: campos `private` y acceso mediante métodos; así el objeto protege sus reglas (por ejemplo, un contador que no pase de un límite).
- **Constructor**: inicializa el objeto. `this` se refiere al objeto actual.
- **Herencia**: `class Perro extends Animal` hereda campos y métodos. `super(...)` llama al constructor del padre y `super.metodo()` a su versión.
- **Polimorfismo**: una variable del tipo padre puede apuntar a un hijo, y se ejecuta el método del hijo (`@Override`).
- **`toString`**: texto que representa al objeto al imprimirlo.
- **`equals` y `hashCode`**: definen cuándo dos objetos son iguales. Si sobrescribes uno, sobrescribe el otro con los mismos campos.

## Ejemplo

```java
class Animal {
    protected String nombre;
    Animal(String nombre) { this.nombre = nombre; }
    String sonido() { return "..."; }
    @Override public String toString() { return nombre + " dice " + sonido(); }
}

class Gato extends Animal {
    Gato(String nombre) { super(nombre); }
    @Override String sonido() { return "miau"; }
}

class Punto {
    private final int x, y;
    Punto(int x, int y) { this.x = x; this.y = y; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Punto)) return false;
        Punto p = (Punto) o;
        return x == p.x && y == p.y;
    }
    @Override public int hashCode() { return Objects.hash(x, y); }
}

class Demo {
    static void probar() {
        Animal a = new Gato("Misu");
        System.out.println(a); // imprime: Misu dice miau
        System.out.println(new Punto(1, 2).equals(new Punto(1, 2))); // imprime: true
    }
}
```

## Errores comunes

- Olvidar `@Override` y crear por accidente una sobrecarga en lugar de una sobrescritura.
- Sobrescribir `equals` sin `hashCode`: rompe `HashMap` y `HashSet`.
- Campos `public` que permiten dejar el objeto en un estado inválido.
- No llamar a `super(...)` cuando el padre no tiene constructor sin argumentos.
- Confundir `equals` con `==`.

## En entrevistas

- Sobrecarga contra sobrescritura, y cuándo se resuelve cada una (compilación contra ejecución).
- Contrato entre `equals` y `hashCode`.
- Herencia contra composición: por qué muchos prefieren componer.
- Qué hace `final` en una clase, un método y un campo.
