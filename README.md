# Java Practice Lab

Laboratorio de ejercicios de Java orientado a **entrevistas técnicas**: escribes el código en el
navegador, el backend lo compila y ejecuta los tests JUnit por ti, y tu progreso queda guardado
por cuenta.

Actualmente incluye **50 ejercicios de Nivel Básico** (el plan son ~100 entre Básico e Intermedio).

## Documentación

| Archivo | Contenido |
|---|---|
| [CLAUDE.md](./CLAUDE.md) | **Memoria del proyecto**: estado, decisiones, trampas conocidas y pendientes |
| [NEGOCIO.md](./NEGOCIO.md) | Qué es, para quién y por qué |
| [PLAN.md](./PLAN.md) | Plan por fases y currículo |

---

## Requisitos

| Herramienta | Versión usada | Nota |
|---|---|---|
| Java (JDK) | 17 | Backend, runner-service y los ejercicios de Java |
| Python | 3.12 + pytest | Para los ejercicios de Python (el runner-service lo ejecuta) |
| Node.js | 24.x | Para el frontend (Next.js 16) |
| Maven | — | **No hace falta instalarlo**: se usa el wrapper `mvnw` de la raíz |

---

## Puesta en marcha

### 1. Arrancar el runner-service (el que ejecuta el código)

Necesita JDK (para Java) y Python con pytest (para Python) en el equipo. En un servidor, el
runner-service corre como contenedor con todo eso dentro; en local, usas lo que tengas instalado.

```bash
# desde la raíz del proyecto (reactor: padre + runner-core + runner-service + backend)
JAVALAB_JUNIT_JAR="$PWD/runner/junit-platform-console-standalone.jar" \
  ./mvnw -pl runner-core,runner-service -am spring-boot:run
```

Queda en `http://localhost:8081`.

### 2. Arrancar el backend

```bash
cd backend
cp ../.env.example ../.env      # OBLIGATORIO: rellena JAVALAB_JWT_SECRET (openssl rand -base64 48)
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080/api/v1`. Por defecto llama al runner-service en
`http://localhost:8081` (se cambia con `JAVALAB_RUNNER_SERVICE_URL`).

> La base de datos SQLite se crea sola en `backend/data/javalab.db` (ignorada por git).

### 3. Arrancar el frontend

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

Abre `http://localhost:3000`, crea una cuenta y empieza a resolver ejercicios.

---

## Cómo funciona un envío

1. El frontend manda el código a `POST /api/v1/submissions`.
2. El backend se lo pasa al **runner-service** por HTTP (`POST /internal/run`), con el lenguaje, los
   tests del ejercicio y el código del alumno. El backend **no ejecuta nada**.
3. El runner-service valida el código (lista blanca de imports y lista negra de APIs peligrosas:
   `Runtime`, `ProcessBuilder`, `System.exit`, `java.io`, hilos, sockets…; en Python, `os`,
   `subprocess`, `eval`…), compila y ejecuta **los tests del ejercicio** (que viven solo en el
   servidor), con un timeout de 10 segundos.
4. Devuelve el resultado test por test, y el backend lo guarda en el progreso.

El runner-service es la única pieza que tiene compiladores (JDK, Python). Añadir un lenguaje
(TypeScript, C, C++…) es añadir su toolchain a **su** imagen y su comando; el backend no cambia.

---

## Estructura

```
java_practice_project/
├── backend/         Spring Boot: API, autenticación JWT, catálogo y progreso (NO ejecuta código)
├── runner-core/     Código compartido: validadores, adaptadores de lenguaje y lectura de resultados
├── runner-service/  Servicio que compila y ejecuta el código (Java, Python y los que vengan)
├── frontend/        Next.js 16 + Tailwind + Monaco Editor
├── exercises/       Catálogo de ejercicios en YAML (enunciado, pistas, solución y tests)
├── runner/          El jar de JUnit que usan los ejercicios de Java
└── PLAN.md      Plan del proyecto y fases pendientes
```

---

## Añadir un ejercicio

Crea un archivo en `exercises/level-1/<slug>.yml`:

```yaml
slug: mi-ejercicio
level: 1
topic: arrays
title: Mi ejercicio
statement: |
  Enunciado con ejemplos de entrada -> salida.
methodSignature: "public static int miMetodo(int[] numbers)"
className: Solution
estimatedMinutes: 10
difficulty: easy
hints:
  - "Primera pista"
starterCode: |
  public class Solution {
      public static int miMetodo(int[] numbers) {
          // Tu código aquí
          return 0;
      }
  }
referenceSolution: |
  public class Solution {
      public static int miMetodo(int[] numbers) {
          return numbers.length;
      }
  }
tests: |
  import org.junit.jupiter.api.DisplayName;
  import org.junit.jupiter.api.Test;

  import static org.junit.jupiter.api.Assertions.*;

  class SolutionTest {

      @Test
      @DisplayName("Cuenta los elementos")
      void cuentaElementos() {
          assertEquals(3, Solution.miMetodo(new int[] {1, 2, 3}));
      }
  }
```

Reglas: la clase siempre se llama `Solution`, el método es `public static`, la clase de tests es
`SolutionTest` y `referenceSolution` (no `starterCode`) debe hacer pasar todos los tests.
Al arrancar, el backend valida los YAML y registra en el log los que tengan problemas.

Para validar todo el catálogo de una pasada: `python3 verificar_ejercicios.py` desde la raíz.

---

## Pruebas

```bash
cd backend
./mvnw test          # 43 tests: catálogo, validación de código, JWT y flujo completo con envíos reales

cd frontend
npx tsc --noEmit     # tipos
npm run build        # build de producción

# Desde la raíz: valida los 50 ejercicios (la solución de referencia pasa y el
# starterCode falla), compilando y ejecutando JUnit de verdad.
python3 verificar_ejercicios.py
```

Los tests del backend incluyen envíos reales: compilan y ejecutan los ejercicios con el motor
configurado, así que tardan unos segundos.

---

## API

Todas las respuestas siguen el contrato LRCH:

```jsonc
// éxito
{ "success": true, "data": { }, "message": "..." }
// error
{ "success": false, "error": "CODIGO_EN_MAYUSCULAS", "message": "..." }
// lista paginada
{ "success": true, "data": [ ], "pagination": { "total": 0, "page": 1, "per_page": 20, "total_pages": 0 } }
```

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/auth/register` | Crear cuenta (deja la sesión iniciada) |
| POST | `/auth/login` | Iniciar sesión |
| POST | `/auth/logout` | Cerrar sesión |
| GET | `/auth/me` | Usuario actual |
| GET | `/levels` | Niveles con el avance del usuario |
| GET | `/exercises?level=&topic=&status=&search=&page=&per_page=` | Lista paginada |
| GET | `/exercises/{slug}` | Detalle (nunca incluye la solución) |
| GET | `/exercises/{slug}/hints/{index}` | Pista concreta |
| POST | `/submissions` | Compilar, ejecutar los tests y guardar el intento |
| GET | `/progress` | XP, resueltos, racha y avance por nivel |
| GET | `/progress/history` | Historial de envíos |
| GET | `/health` | Estado del servicio (público) |

---

## Despliegue (Coolify + Docker + Neon + frontend)

Dos servicios en Coolify: el **backend** (API, sin Docker) y el **runner-service** (compila y ejecuta).

```
Navegador ──► Frontend (Next.js) ──/api/v1/*──► Backend (Coolify)
                                                    │   │
                                                    │   └─► Neon (Postgres) + Resend (correos)
                                                    │
                                                    └─► Runner-service (red interna, /internal/run)
                                                            └─► compila y ejecuta (JDK, Python)
```

El backend **no ejecuta nada**: manda el código al runner-service por HTTP. El runner-service es la
única pieza con compiladores. Se comunican por la red interna de Coolify, sin socket ni Docker.

**1. Base de datos (Neon).** Crea un proyecto y copia host, base, usuario y contraseña. La URL de Neon
empieza por `postgresql://`; el backend necesita el formato JDBC:
`jdbc:postgresql://<host>/<base>?sslmode=require`, con usuario y contraseña en variables aparte.

**2. Runner-service.** Nuevo servicio en Coolify:
- Build: `Dockerfile Path = ./runner-service/Dockerfile`, contexto = raíz del repo.
- Puerto interno `8081`.
- Health Check: `/internal/health`.
- **No se expone a internet** (solo lo llama el backend por la red interna).

**3. Backend.** Dockerfile Path = `./Dockerfile`, Health Check = `/api/v1/health`. Variables:

| Variable | Valor |
|---|---|
| `JAVALAB_JWT_SECRET` | aleatorio (`openssl rand -base64 48`), distinto al local |
| `JAVALAB_COOKIE_SECURE` | `true` |
| `JAVALAB_ALLOWED_ORIGINS` | el origen del frontend (nunca `*`) |
| `JAVALAB_DB_URL` / `JAVALAB_DB_USER` / `JAVALAB_DB_PASSWORD` | los de Neon (formato JDBC) |
| `JAVALAB_RESEND_API_KEY` / `JAVALAB_MAIL_FROM` | los de Resend |
| `JAVALAB_SUPER_ADMIN_EMAIL` | tu correo: esa cuenta se promueve a SUPER_ADMIN al arrancar |
| `JAVALAB_RUNNER_SERVICE_URL` | `http://<nombre-del-servicio-runner>:8081` (red interna de Coolify) |

Los envíos se ejecutan de uno en uno (`JAVALAB_RUNNER_MAX_CONCURRENT=1`) para acotar CPU; el resto
espera su turno hasta `JAVALAB_RUNNER_QUEUE_WAIT_SECONDS` y si se agota responde 429 `SERVIDOR_OCUPADO`.

El `Dockerfile` ya fija `SPRING_PROFILES_ACTIVE=prod`; en producción el **registro está cerrado por
defecto** (`JAVALAB_REGISTRATION_ENABLED=false`): las cuentas se crean por invitación.

**4. Frontend.** Variables (definirlas **antes** del build, porque `NEXT_PUBLIC_*` se incrusta):

| Variable | Valor |
|---|---|
| `NEXT_PUBLIC_API_BASE_URL` | `/api/v1` |
| `BACKEND_URL` | la URL del backend (sin barra final) |

La key de Resend y el secreto JWT van **solo** en el backend; lo que empieza por `NEXT_PUBLIC_` lo ve cualquiera.

**Cuentas por invitación.** El SUPER_ADMIN entra a `/admin`, escribe el correo de la persona y el sistema crea la
cuenta con una **contraseña temporal aleatoria** que llega por correo (con la indicación de que puede cambiarla desde
Perfil → Cambiar contraseña). Desde el mismo panel se reenvía una contraseña nueva o se desactiva una cuenta. La
contraseña temporal solo viaja en ese correo; la API nunca la devuelve. El rol se comprueba contra la base de datos en
cada llamada, no contra el token.

**Sobre la seguridad del runner.** El código del alumno **no se ejecuta en el backend**: corre dentro del
runner-service, con un usuario sin privilegios, un directorio temporal que se borra al acabar y un timeout
de 10 segundos. La validación del código (imports y APIs prohibidas) ocurre **antes** de compilar. El
registro está cerrado: la única puerta de entrada son las invitaciones.

## Seguridad

- La sesión viaja en una **cookie httpOnly** (`SameSite=Lax`); el token nunca se guarda en
  `localStorage`.
- Las contraseñas se guardan con **BCrypt**.
- Los tests y las soluciones de referencia **nunca** se envían al cliente.
- El código del alumno se ejecuta en un contenedor sin red, sin capacidades y con el sistema de
  archivos de solo lectura.
- Los errores no exponen stack traces ni detalles internos.

### Antes de subirlo a un repositorio

- Define `JAVALAB_JWT_SECRET` con un valor aleatorio de al menos 32 caracteres y pon
  `JAVALAB_COOKIE_SECURE=true` (obliga a HTTPS).
- Para que lleguen los correos (códigos de recuperación y bienvenida) define `JAVALAB_RESEND_API_KEY` y
  `JAVALAB_MAIL_FROM` (por ejemplo `Java Lab <no-reply@mail.luisrodrigoch.lat>`, dominio verificado en Resend).
  Sin la API key no se envía nada: el correo y su código salen en el log del backend.
- No subas `.env` ni `.env.local` (ya están ignorados).
- `npm audit` reporta 5 vulnerabilidades altas en `eslint-config-next` (dependencias de desarrollo
  transitivas vía `braces`). No afectan al código desplegado; se resolverán cuando el paquete lo
  actualice.
