/** Traduce los identificadores tecnicos del catalogo a texto para la pantalla. */

const TEMAS_PREGUNTAS: Record<string, string> = {
  "java-basics": "Java básico",
  oop: "Programación orientada a objetos",
  exceptions: "Excepciones",
  collections: "Colecciones",
  generics: "Genéricos",
  "streams-lambdas": "Streams y lambdas",
  concurrency: "Concurrencia",
  "jvm-memory": "JVM y memoria",
  "design-patterns": "Patrones de diseño",
  testing: "Pruebas (JUnit y Mockito)",
  "spring-core": "Spring Core (IoC y DI)",
  "spring-boot": "Spring Boot y REST",
  "spring-data-jpa": "Spring Data JPA",
  "databases-sql": "Bases de datos y SQL",
};

const DIFICULTADES: Record<string, string> = {
  easy: "Fácil",
  medium: "Media",
  hard: "Difícil",
};

const ESTADOS: Record<string, string> = {
  PASSED: "Correcto",
  FAILED: "Falló",
  COMPILE_ERROR: "Error de compilación",
  TIMEOUT: "Sin tiempo",
  INTERNAL_ERROR: "Error interno",
};

export function nombreDeTemaDePreguntas(tema: string): string {
  return TEMAS_PREGUNTAS[tema] ?? tema;
}

export function nombreDeDificultad(dificultad: string | null): string {
  if (!dificultad) {
    return "Sin definir";
  }
  return DIFICULTADES[dificultad] ?? dificultad;
}

export function nombreDeEstado(estado: string): string {
  return ESTADOS[estado] ?? estado;
}

/** "capitalizeWords" -> "Capitalize words" para mostrar nombres de tests. */
export function nombreDeTest(nombre: string): string {
  const conEspacios = nombre
    .replace(/([a-z0-9])([A-Z])/g, "$1 $2")
    .replace(/_/g, " ")
    .trim();
  return conEspacios.charAt(0).toUpperCase() + conEspacios.slice(1);
}

export function formatearFecha(iso: string): string {
  const fecha = new Date(iso);
  return new Intl.DateTimeFormat("es-MX", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(fecha);
}

export function formatearDuracion(milisegundos: number): string {
  if (milisegundos < 1000) {
    return `${milisegundos} ms`;
  }
  return `${(milisegundos / 1000).toFixed(1)} s`;
}
