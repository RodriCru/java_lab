import { NextResponse, type NextRequest } from "next/server";
import { COOKIE_SESION } from "@/lib/api";

/**
 * En Next.js 16 el antiguo middleware se llama proxy.
 *
 * Solo hace una comprobacion optimista: mira si existe la cookie de sesion. La validacion
 * real la hace el backend en cada peticion, que responde 401 si el token caduco.
 */
const RUTAS_PUBLICAS = ["/login", "/registro", "/olvide-contrasena"];

export default function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const tieneSesion = Boolean(request.cookies.get(COOKIE_SESION)?.value);
  const esRutaPublica = RUTAS_PUBLICAS.some(
    (ruta) => pathname === ruta || pathname.startsWith(`${ruta}/`),
  );

  if (!tieneSesion && !esRutaPublica) {
    const destino = new URL("/login", request.url);
    // El parametro `sesion` distingue entrar sin sesion de una sesion que caduco: mismo destino, aviso distinto.
    destino.searchParams.set("sesion", "requerida");
    if (pathname !== "/") {
      destino.searchParams.set("destino", pathname);
    }
    return NextResponse.redirect(destino);
  }

  if (tieneSesion && esRutaPublica) {
    return NextResponse.redirect(new URL("/", request.url));
  }

  return NextResponse.next();
}

export const config = {
  // /api/ queda fuera: son las llamadas al backend (rewrite) y el propio backend responde 401 si falta la sesion.
  matcher: ["/((?!api/|_next/static|_next/image|favicon.ico|.*\\.(?:png|jpg|jpeg|svg|ico)$).*)"],
};
