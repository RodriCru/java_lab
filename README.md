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
| Java (JDK) | 17 | El backend usa Java 17 y Spring Boot 3.5 |
| Docker | 29.x | Necesario para el motor de ejecución aislado |
| Node.js | 24.x | Para el frontend (Next.js 16) |
| Maven | — | **No hace falta instalarlo**: se usa el wrapper `backend/mvnw` |

---

## Puesta en marcha

### 1. Construir la imagen que ejecuta el código de los alumnos

Solo la primera vez (y cuando cambie `runner/`):

```bash
cd runner
./build.sh            # crea la imagen javalab-runner:17
```

### 2. Arrancar el backend

```bash
cd backend
cp ../.env.example ../.env      # OBLIGATORIO: rellena JAVALAB_JWT_SECRET (openssl rand -base64 48)
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080/api/v1`.

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
2. El backend revisa el código contra una lista blanca de imports y una lista negra de APIs
   peligrosas (`Runtime`, `ProcessBuilder`, `System.exit`, `java.io`, hilos, sockets…).
3. Se inyectan los tests JUnit del ejercicio, que **viven solo en el servidor**.
4. Se compila y ejecuta dentro de un contenedor desechable:
   `--network none --memory 256m --cpus 0.5 --pids-limit 64 --cap-drop ALL --read-only`,
   con un timeout de 10 segundos.
5. Se devuelve el resultado test por test y se guarda el intento en el progreso.

Si Docker no está disponible, el backend cae automáticamente al **motor local** (`javac` del
equipo) y lo avisa en los logs. El aislamiento es menor, por eso se puede desactivar con
`JAVALAB_RUNNER_USE_DOCKER=false`.

---

## Estructura

```
java_practice_project/
├── backend/     Spring Boot: API, autenticación JWT, catálogo y motor de ejecución
├── frontend/    Next.js 16 + Tailwind + Monaco Editor
├── exercises/   Catálogo de ejercicios en YAML (enunciado, pistas, solución y tests)
├── runner/      Imagen Docker que compila y ejecuta el código
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

## Despliegue (Render + Neon + Vercel)

```
Navegador ──► Vercel (Next.js) ──rewrite /api/v1/*──► Render (Docker, Spring Boot) ──► Neon (Postgres)
```

El navegador solo habla con Vercel; Next reenvía `/api/v1/*` al backend. Así la cookie de sesión queda en un
único dominio y `SameSite=Lax` sigue funcionando.

**1. Base de datos (Neon).** Crea un proyecto en neon.tech y copia host, base, usuario y contraseña. La URL
de Neon empieza por `postgresql://`; el backend necesita el formato JDBC:
`jdbc:postgresql://<host>/<base>?sslmode=require`, con usuario y contraseña en variables aparte.

**2. Backend (Render, Web Service con Docker).** Repositorio de GitHub, `Dockerfile Path` = `./Dockerfile`,
Root Directory vacío, Health Check Path = `/api/v1/health`. Variables:

| Variable | Valor |
|---|---|
| `JAVALAB_JWT_SECRET` | aleatorio (`openssl rand -base64 48`), distinto al local |
| `JAVALAB_COOKIE_SECURE` | `true` |
| `JAVALAB_ALLOWED_ORIGINS` | `https://tu-app.vercel.app` |
| `JAVALAB_DB_URL` / `JAVALAB_DB_USER` / `JAVALAB_DB_PASSWORD` | los de Neon (formato JDBC) |
| `JAVALAB_RESEND_API_KEY` / `JAVALAB_MAIL_FROM` | los de Resend |
| `JAVALAB_SUPER_ADMIN_EMAIL` | tu correo: esa cuenta se promueve a SUPER_ADMIN al arrancar |

Los envíos se ejecutan **de uno en uno** (`JAVALAB_RUNNER_MAX_CONCURRENT=1`): cada compilación y ejecución de Java usa
~150–200 MB y el plan gratis de Render tiene 512 MB. Si hay otros en curso, el siguiente espera su turno hasta
60 s; pasado ese tiempo responde 429 `SERVIDOR_OCUPADO`.

El `Dockerfile` ya fija `SPRING_PROFILES_ACTIVE=prod` y `JAVALAB_RUNNER_USE_DOCKER=false`. En producción el
**registro está cerrado por defecto** (`JAVALAB_REGISTRATION_ENABLED=false`): las cuentas se crean por invitación. El plan gratis de
Render se duerme tras ~15 minutos sin tráfico: el primer acceso tarda cerca de un minuto.

**3. Frontend (Vercel).** Root Directory = `frontend`. Variables (definirlas **antes** del primer build,
porque `NEXT_PUBLIC_*` se incrusta al compilar):

| Variable | Valor |
|---|---|
| `NEXT_PUBLIC_API_BASE_URL` | `/api/v1` |
| `BACKEND_URL` | `https://tu-api.onrender.com` (sin barra final) |

La key de Resend y el secreto JWT van **solo** en Render; lo que empieza por `NEXT_PUBLIC_` lo ve cualquiera.

**Cuentas por invitación.** El SUPER_ADMIN entra a `/admin`, escribe el correo de la persona y el sistema crea la
cuenta con una **contraseña temporal aleatoria** que llega por correo (con la indicación de que puede cambiarla desde
Perfil → Cambiar contraseña). Desde el mismo panel se reenvía una contraseña nueva o se desactiva una cuenta. La
contraseña temporal solo viaja en ese correo; la API nunca la devuelve. El rol se comprueba contra la base de datos en
cada llamada, no contra el token.

**Ojo con la seguridad del runner.** En Render no hay Docker, así que el código del alumno se compila y
ejecuta **dentro del mismo contenedor que la API**. Mitigaciones: entorno vacío y `-Xmx128m` para el proceso
del alumno, usuario sin privilegios y el filtro de `SourceValidator`. No es un aislamiento real: por eso
conviene mantener el registro cerrado e invitar solo a gente de confianza.

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
