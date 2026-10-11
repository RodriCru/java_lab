"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { User } from "@/lib/types";

const RUTAS_PUBLICAS = ["/login", "/registro", "/olvide-contrasena"];

const ENLACES = [
  { href: "/", etiqueta: "Inicio" },
  { href: "/niveles", etiqueta: "Ejercicios" },
  { href: "/preguntas", etiqueta: "Preguntas" },
  { href: "/repaso", etiqueta: "Repaso" },
  { href: "/perfil", etiqueta: "Perfil" },
];

export function BarraNavegacion() {
  const pathname = usePathname();
  const router = useRouter();
  const [usuario, setUsuario] = useState<User | null>(null);
  const [saliendo, setSaliendo] = useState(false);

  const esPublica = RUTAS_PUBLICAS.some((ruta) => pathname.startsWith(ruta));

  useEffect(() => {
    if (esPublica) {
      setUsuario(null);
      return;
    }

    let activo = true;
    api
      .usuarioActual()
      .then((respuesta) => {
        if (activo) {
          setUsuario(respuesta.data);
        }
      })
      .catch((error: unknown) => {
        // 401 ya lo maneja el proxy; aqui solo evitamos ruido.
        if (!(error instanceof ApiError && error.sesionExpirada)) {
          console.error(error);
        }
      });

    return () => {
      activo = false;
    };
  }, [esPublica, pathname]);

  if (esPublica) {
    return null;
  }

  async function cerrarSesion() {
    setSaliendo(true);
    try {
      await api.cerrarSesion();
    } finally {
      setSaliendo(false);
      router.push("/login");
      router.refresh();
    }
  }

  return (
    <header className="border-b border-zinc-200 bg-white">
      <nav className="mx-auto flex w-full max-w-6xl items-center justify-between gap-4 px-4 py-3">
        <div className="flex items-center gap-6">
          <Link href="/" className="flex items-center gap-2 font-semibold text-zinc-900">
            <span className="grid h-7 w-7 place-items-center rounded-md bg-indigo-600 text-xs font-bold text-white">
              J
            </span>
            Java Practice Lab
          </Link>
          <ul className="hidden items-center gap-1 sm:flex">
            {ENLACES.map((enlace) => {
              const activo =
                enlace.href === "/" ? pathname === "/" : pathname.startsWith(enlace.href);
              return (
                <li key={enlace.href}>
                  <Link
                    href={enlace.href}
                    aria-current={activo ? "page" : undefined}
                    className={`rounded-md px-3 py-1.5 text-sm font-medium transition-colors ${
                      activo
                        ? "bg-indigo-50 text-indigo-700"
                        : "text-zinc-600 hover:bg-zinc-100 hover:text-zinc-900"
                    }`}
                  >
                    {enlace.etiqueta}
                  </Link>
                </li>
              );
            })}
          </ul>
        </div>

        <div className="flex items-center gap-3">
          {usuario ? (
            <span className="hidden text-sm text-zinc-600 md:inline">
              {usuario.displayName}
            </span>
          ) : null}
          <button
            type="button"
            onClick={cerrarSesion}
            disabled={saliendo}
            className="rounded-md border border-zinc-300 px-3 py-1.5 text-sm font-medium text-zinc-700 transition-colors hover:bg-zinc-100 disabled:opacity-60"
          >
            {saliendo ? "Saliendo…" : "Cerrar sesión"}
          </button>
        </div>
      </nav>
    </header>
  );
}
