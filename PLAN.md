# Plan — Java Practice Lab

Laboratorio de ejercicios de Java orientado a **entrevistas técnicas**: ~100 ejercicios de lógica por niveles básico e intermedio, con backend, frontend, login multiusuario y ejecución real del código del alumno.

**Estado:** revisado con feedback del dev (login + ~100 ejercicios orientados a entrevista) — pendiente de OK final
**Fecha:** 2026-10-08

---

## 1. Decisiones tomadas

| Tema | Decisión |
|---|---|
| Ejecución del código | Runner con Docker: contenedor efímero por envío |
| Usuarios | Multiusuario con registro/login; sesión JWT en cookie `httpOnly`; progreso por usuario |
| Enfoque del contenido | Entrevista técnica: lógica, estructuras de datos y resolución de problemas |
| Niveles | Básico (~50) + Intermedio (~50) = **~100 ejercicios** |
| Backend | Java 17 + Spring Boot 3.5.16 + Spring Security |
| Frontend | Next.js 16 + TypeScript + Tailwind CSS + Monaco Editor (rutas protegidas en `proxy.ts`) |
| Base de datos | SQLite en desarrollo (vía JPA) |
| Build Java | Maven Wrapper (`mvnw`) — no hay Maven global en el sistema |
| Entorno verificado | Java 17.0.17 LTS · Node 24.14 · npm 11.14 · Docker 29.7 · Python 3.14 |

---

## 2. Estructura del monorepo

```
java_practice_project/
├── backend/                      Spring Boot (API + auth + motor de ejecución)
│   ├── src/main/java/.../auth/       registro, login, JWT, filtros de seguridad
│   ├── src/main/java/.../users/      entidad User, perfil, soft delete
│   ├── src/main/java/.../api/        controllers, DTOs, manejo global de errores
│   ├── src/main/java/.../runner/     orquestación Docker, timeouts, whitelist
│   ├── src/main/java/.../catalog/    carga de ejercicios YAML
│   ├── src/main/java/.../progress/   intentos, resueltos, XP y racha por usuario
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── mvnw
│   └── pom.xml
├── frontend/                     Next.js 16 + TS + Tailwind + Monaco
├── exercises/                    Catálogo YAML + tests JUnit por ejercicio
├── runner/                       Dockerfile de la imagen de ejecución
├── docker-compose.yml
└── PLAN.md
```

---

## 3. Motor de ejecución (el corazón del proyecto)

Flujo de un envío:

1. `POST /api/v1/submissions` recibe `{ exerciseSlug, sourceCode }`.
2. El backend valida el código contra una **whitelist de imports**. Se bloquean: `Runtime`, `ProcessBuilder`, `java.io.File`, `java.net`, `System.exit`, `Thread` sin control.
3. Se inyecta el código del alumno junto con los tests JUnit del ejercicio. Los tests viven en `exercises/` y **nunca viajan al navegador**.
4. Se lanza un contenedor efímero desde la imagen de `runner/` con aislamiento:
   - `--network none`
   - `--memory 256m`
   - `--cpus 0.5`
   - `--read-only`
   - timeout de 10 segundos
5. Se devuelve el resultado por test y se registra el intento (resuelto o fallido) en el progreso del usuario autenticado.

**Fallback** si Docker no está disponible: `javax.tools.JavaCompiler` en un proceso hijo aislado, con el mismo contrato de respuesta, para que el laboratorio nunca quede inservible.

---

## 4. Contrato de API

```json
// Éxito
{ "success": true, "data": { ... }, "message": "..." }

// Error
{ "success": false, "error": "CODIGO_EN_MAYUSCULAS", "message": "..." }

// Lista paginada
{ "success": true, "data": [ ... ], "pagination": { "total": 0, "page": 1, "per_page": 20, "total_pages": 0 } }
```

### Endpoints (`/api/v1`)

### Autenticación

| Método | Ruta | Función |
|---|---|---|
| POST | `/auth/register` | Crea la cuenta (email + password con BCrypt) |
| POST | `/auth/login` | Inicia sesión y entrega el JWT en cookie `httpOnly` |
| POST | `/auth/logout` | Invalida la sesión |
| GET | `/auth/me` | Devuelve el usuario autenticado actual |

### Catálogo y ejercicios

| Método | Ruta | Función |
|---|---|---|
| GET | `/levels` | Niveles, conteo de ejercicios y porcentaje de avance del usuario |
| GET | `/exercises?level=&topic=&page=` | Lista paginada de ejercicios |
| GET | `/exercises/{slug}` | Enunciado, starter code y pistas (**sin** la solución) |
| GET | `/exercises/{slug}/hints/{index}` | Revela la pista indicada |
| POST | `/submissions` | Compila y ejecuta los tests → resultado |

### Progreso

| Método | Ruta | Función |
|---|---|---|
| GET | `/progress` | Resueltos, XP, racha y pendientes |
| GET | `/progress/history?page=` | Historial de intentos paginado |

---

## 5. Frontend

| Ruta | Pantalla |
|---|---|
| `/login` | Inicio de sesión |
| `/registro` | Crear cuenta |
| `/` | Dashboard: progreso general, XP, racha |
| `/niveles` | Lista de niveles con avance |
| `/niveles/[level]` | Ejercicios del nivel, filtrados por tema |
| `/ejercicios/[slug]` | Enunciado + editor Monaco + salida + resultados de tests + pistas |
| `/repaso` | Ejercicios fallados o incompletos |
| `/perfil` | Datos de la cuenta e historial de intentos |

El archivo `proxy.ts` (el antiguo middleware de Next.js) protege todas las rutas excepto `/login` y `/registro`. Todas las vistas incluyen estados de carga, vacío y error.

---

## 6. Currículo (~100 ejercicios orientados a entrevista técnica)

El objetivo no es aprender Java en abstracto, sino entrenar **lo que realmente se pregunta en una entrevista técnica**: resolver problemas con lógica, elegir la estructura de datos correcta y saber analizar la complejidad de la solución.

Todo ejercicio se resuelve escribiendo **un método** con la firma ya dada. Los tests JUnit validan casos normales, casos borde y casos extremos (vacíos, negativos, listas grandes).

### Nivel 1 — Básico (50 ejercicios) ✅ completo

- **Lógica y matemáticas (11):** par o impar, mayor de tres números, factorial, suma de dígitos, ¿es primo?, primos hasta un límite, MCD, mcm, FizzBuzz, ¿número de Armstrong?, invertir un número.
- **Cadenas de texto (9):** invertir, palíndromo, contar vocales, contar palabras, anagramas, capitalizar, formato de título, invertir palabras, primera letra no repetida.
- **Arreglos (9):** máximo, suma y promedio, invertir, contar ocurrencias, eliminar duplicados, segundo mayor, ¿está ordenado?, combinar ordenados, rotar a la izquierda.
- **Matrices y patrones (3):** suma de matriz, transponer, diagonal principal.
- **Bucles y recursión simple (3):** tabla de multiplicar, potencia, contar pares.
- **Estructuras de datos (4):** contar frecuencias con `Map`, sumar una lista, palabra más frecuente, corchetes balanceados.
- **Objetos y clases (3):** ordenar personas (dos arreglos paralelos), un `record` para dinero, `equals` + `hashCode` en una clase.
- **Recursión (5):** suma recursiva, invertir recursivamente, cuenta regresiva, Fibonacci, Torres de Hanoi.
- **Algoritmos (3):** búsqueda binaria, ordenamiento burbuja, ordenamiento por selección.

### Nivel 2 — Intermedio (~50 ejercicios)

- **Estructuras de datos:** contar frecuencias con `Map`, validar paréntesis balanceados, detectar duplicados con `Set`, agrupar por clave, primer elemento repetido, subarray con suma dada.
- **Dos punteros y ventana deslizante:** par que suma un objetivo en array ordenado, quitar duplicados in-place, elemento mayoritario, mover negativos, ventana deslizante básica.
- **Strings intermedios:** substring más largo sin caracteres repetidos, prefijo común más largo, contar subcadenas, conteo de palabras con `Map`, rotación de strings.
- **Recursión y algoritmos:** búsqueda binaria, ordenamiento burbuja y por selección, merge de arrays ordenados, torres de Hanoi, generar permutaciones.
- **POO y colecciones:** clase `Cuenta`, jerarquía de `Empleado` con herencia, `Comparable` y `Comparator`, inmutabilidad, `record`, `equals` / `hashCode`.
- **Java moderno:** Streams básicos (`filter`, `map`, `collect`), `Optional`, lambdas.
- **Análisis de complejidad:** cada ejercicio de este nivel incluye una pregunta sobre la complejidad de la solución propuesta, que el alumno responde en el mismo envío.

### Modelo de datos de un ejercicio

Cada ejercicio es un YAML en `exercises/level-<n>/` con esta forma (claves en inglés, como el
resto del código):

```yaml
slug: sum-of-digits
level: 1
topic: logic-math
title: Suma de dígitos
statement: |
  Enunciado con ejemplos de entrada -> salida.
methodSignature: "public static int sumOfDigits(int number)"
className: Solution          # siempre Solution
estimatedMinutes: 10
difficulty: easy
hints:
  - "Primera pista"
starterCode: |
  public class Solution { }
referenceSolution: |         # solo en el servidor
  public class Solution { }
tests: |                     # JUnit, solo en el servidor
  class SolutionTest { }
complexityQuestion:          # obligatoria en los ejercicios de Nivel 2 (opción múltiple)
  prompt: "¿Cuál es la complejidad temporal de tu solución?"
  options: ["O(1)", "O(n)", "O(n log n)", "O(n²)"]
  correctIndex: 1            # solo servidor; se revela tras pasar los tests
  explanation: "Por qué."
```

---

## 7. Calidad y validación

- JUnit 5 para los tests del backend.
- Tests de autenticación: registro, login y acceso denegado a rutas protegidas sin sesión.
- `python3 verificar_ejercicios.py` para validar el catálogo completo (ref pasa, starter falla).
- `npx tsc --noEmit` obligatorio antes de dar por cerrado cualquier cambio en el front.
- `npm run build` para validar el frontend antes de cerrar una fase.
- Vitest + Playwright para el frontend (previsto para cuando haya lógica que probar; hoy la
  verificación del front es `tsc --noEmit` + `npm run build`).
- Verificación end-to-end: un envío correcto pasa, uno incorrecto falla con feedback claro (qué test falló y por qué, sin exponer la solución ni stack traces).

---

## 8. Fases de entrega

| Fase | Alcance | Entregable verificable | Estado |
|---|---|---|---|
| **1 — Núcleo** | Auth completo (registro, login, JWT en cookie), API de catálogo, runner Docker, front con login + editor, y 20 ejercicios del Nivel 1 | Me registro, inicio sesión, resuelvo un ejercicio y veo mis tests en verde | ✅ **Completada** |
| **2 — Nivel Básico** | Completar el Nivel 1 hasta 50 ejercicios + pulir el dashboard | 50 ejercicios navegables con progreso persistido por usuario | ✅ **Completada** |
| **3 — Nivel Intermedio** | Nivel 2 completo (50) + análisis de complejidad en cada ejercicio | ~100 ejercicios con seguimiento completo por usuario | ✅ **Completada** |

### 8.1 Lo que ya funciona (Fases 1 y 2)

- **50 ejercicios de Nivel Básico** en 9 temas, cada uno con ~6 tests JUnit verificados
  (295 tests en total, con la solución de referencia pasando y el `starterCode` fallando).
- Registro/login con JWT en cookie `httpOnly` y rutas protegidas por `proxy.ts`.
- Envío de soluciones: compilación y ejecución real en un contenedor Docker aislado (con
  respaldo automático al motor local si Docker no está disponible).
- Progreso por usuario: intentos, historial, XP, racha y avance por nivel.
- Interfaz completa: dashboard, niveles, lista filtrable por tema/estado/búsqueda, editor Monaco
  con pistas y resultado test por test, repaso de fallados y perfil.
- 43 tests en el backend (incluidos envíos reales) y build de producción del frontend.
- Script `verificar_ejercicios.py` para validar todo el catálogo de una pasada.

---

## 9. Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Ejecutar código del usuario es peligroso | Docker sin red, read-only, límites de CPU/memoria y whitelist de imports |
| Docker no disponible en el equipo | Fallback con `JavaCompiler` en proceso aislado |
| Contenedor lento en cada envío → mala experiencia | Imagen precompilada con dependencias JUnit ya en caché |
| Sin Maven global instalado | Maven Wrapper incluido en el repo |
| Fuga de soluciones al cliente | La solución vive solo en el servidor; el endpoint devuelve únicamente el resultado de los tests |
| Contraseñas y sesiones mal manejadas | BCrypt para el hash; JWT firmado en cookie `httpOnly` + `Secure` + `SameSite`; nunca tokens en `localStorage` |
| Firma del método del alumno != firma esperada por los tests | Se valida la firma declarada (`methodSignature`) antes de compilar y el código debe declarar la clase `Solution` |
