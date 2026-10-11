import type {
  AdminUser,
  Attempt,
  Envelope,
  ExerciseDetail,
  ExerciseSummary,
  Hint,
  Level,
  ProgressSummary,
  QuestionAnswer,
  QuestionTopic,
  QuestionView,
  SubmissionResult,
  User,
} from "./types";

export const BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1";

export const COOKIE_SESION = "javalab_token";

/** Nombre visible de cada nivel, igual que lo devuelve el backend en /levels. */
export function nombreDeNivel(level: number): string {
  return `Nivel ${level}`;
}

/** Error con el codigo del contrato LRCH. */
export class ApiError extends Error {
  readonly code: string;
  readonly status: number;

  constructor(code: string, message: string, status: number) {
    super(message);
    this.name = "ApiError";
    this.code = code;
    this.status = status;
  }

  get sesionExpirada(): boolean {
    return this.status === 401;
  }
}

const RUTAS_SIN_SESION = ["/login", "/registro", "/olvide-contrasena"];

/**
 * Si el token caduca con la pantalla abierta, el backend responde 401 SESION_REQUERIDA.
 * Solo ese codigo cuenta: un 401 CREDENCIALES_INVALIDAS del login no debe sacar al usuario.
 * Solo en el navegador (en el servidor no hay donde redirigir).
 */
function redirigirSiSesionCaducada(status: number, codigo?: string): void {
  if (typeof window === "undefined" || status !== 401 || codigo !== "SESION_REQUERIDA") {
    return;
  }
  const { pathname } = window.location;
  if (RUTAS_SIN_SESION.some((ruta) => pathname.startsWith(ruta))) {
    return;
  }
  window.location.assign("/login");
}

async function peticion<T>(path: string, init?: RequestInit): Promise<Envelope<T>> {
  let respuesta: Response;

  try {
    respuesta = await fetch(`${BASE_URL}${path}`, {
      ...init,
      // Necesario para que viaje la cookie httpOnly de sesion.
      credentials: "include",
      cache: "no-store",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
        ...init?.headers,
      },
    });
  } catch {
    throw new ApiError(
      "SIN_CONEXION",
      "No se pudo contactar con el servidor. ¿Está arrancado el backend?",
      0,
    );
  }

  const texto = await respuesta.text();
  let cuerpo: Envelope<T> | null = null;

  if (texto) {
    try {
      cuerpo = JSON.parse(texto) as Envelope<T>;
    } catch {
      throw new ApiError("RESPUESTA_INVALIDA", "El servidor devolvió una respuesta ilegible.", respuesta.status);
    }
  }

  if (!respuesta.ok || !cuerpo?.success) {
    redirigirSiSesionCaducada(respuesta.status, cuerpo?.error);
    throw new ApiError(
      cuerpo?.error ?? "ERROR_DESCONOCIDO",
      cuerpo?.message ?? "Ocurrió un error inesperado.",
      respuesta.status,
    );
  }

  return cuerpo;
}

async function obtener<T>(path: string): Promise<Envelope<T>> {
  return peticion<T>(path);
}

async function enviar<T>(path: string, body: unknown): Promise<Envelope<T>> {
  return peticion<T>(path, { method: "POST", body: JSON.stringify(body) });
}

function aQuery(filtros: Record<string, string | number | undefined>): string {
  const params = new URLSearchParams();
  Object.entries(filtros).forEach(([clave, valor]) => {
    if (valor !== undefined && valor !== "") {
      params.set(clave, String(valor));
    }
  });
  const cadena = params.toString();
  return cadena ? `?${cadena}` : "";
}

export const api = {
  // --- Autenticacion ---
  registrar: (datos: { email: string; password: string; displayName?: string }) =>
    enviar<User>("/auth/register", datos),

  iniciarSesion: (datos: { email: string; password: string }) =>
    enviar<User>("/auth/login", datos),

  cerrarSesion: () => enviar<null>("/auth/logout", {}),

  estadoDelRegistro: () => obtener<{ enabled: boolean }>("/auth/registration-status"),

  cambiarContrasena: (datos: { currentPassword: string; newPassword: string }) =>
    enviar<null>("/auth/me/password", datos),

  // --- Administracion (solo SUPER_ADMIN) ---
  adminUsuarios: () => obtener<AdminUser[]>("/admin/users"),

  adminInvitar: (datos: { email: string; displayName?: string }) =>
    enviar<AdminUser>("/admin/invitations", datos),

  adminReenviar: (id: number) => enviar<AdminUser>(`/admin/users/${id}/resend-invitation`, {}),

  adminCambiarEstado: (id: number, active: boolean) =>
    enviar<AdminUser>(`/admin/users/${id}/active`, { active }),

  usuarioActual: () => obtener<User>("/auth/me"),

  // --- Recuperacion de cuenta (codigo OTP por correo) ---
  olvideContrasena: (email: string) => enviar<null>("/auth/forgot-password", { email }),

  restablecerContrasena: (datos: { email: string; code: string; newPassword: string }) =>
    enviar<null>("/auth/reset-password", datos),

  pedirCambioDeCorreo: (datos: { newEmail: string; currentPassword: string }) =>
    enviar<null>("/auth/me/email", datos),

  confirmarCambioDeCorreo: (code: string) => enviar<User>("/auth/me/email/confirm", { code }),

  // --- Catalogo ---
  niveles: () => obtener<Level[]>("/levels"),

  ejercicios: (filtros: {
    level?: number;
    topic?: string;
    status?: string;
    search?: string;
    page?: number;
    perPage?: number;
  }) =>
    obtener<ExerciseSummary[]>(
      `/exercises${aQuery({
        level: filtros.level,
        topic: filtros.topic,
        status: filtros.status,
        search: filtros.search,
        page: filtros.page,
        per_page: filtros.perPage,
      })}`,
    ),

  ejercicio: (slug: string) => obtener<ExerciseDetail>(`/exercises/${slug}`),

  pista: (slug: string, indice: number) => obtener<Hint>(`/exercises/${slug}/hints/${indice}`),

  // --- Envios y progreso ---
  enviarSolucion: (datos: {
    exerciseSlug: string;
    sourceCode: string;
    complexityAnswer?: number | null;
  }) =>
    enviar<SubmissionResult>("/submissions", datos),

  progreso: () => obtener<ProgressSummary>("/progress"),

  // --- Banco de preguntas ---
  temasDePreguntas: () => obtener<QuestionTopic[]>("/questions/topics"),

  preguntas: (filtros: {
    topic?: string;
    difficulty?: string;
    status?: string;
    random?: boolean;
    page?: number;
    perPage?: number;
  }) =>
    obtener<QuestionView[]>(
      `/questions${aQuery({
        topic: filtros.topic,
        difficulty: filtros.difficulty,
        status: filtros.status,
        random: filtros.random ? "true" : undefined,
        page: filtros.page,
        per_page: filtros.perPage,
      })}`,
    ),

  responderPregunta: (id: string, selectedIndex: number) =>
    enviar<QuestionAnswer>(`/questions/${id}/answer`, { selectedIndex }),

  historial: (page = 1, perPage = 20) =>
    obtener<Attempt[]>(`/progress/history${aQuery({ page, per_page: perPage })}`),
};
