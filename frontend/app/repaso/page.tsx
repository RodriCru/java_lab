"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { Estado, EstadoCargando } from "@/components/Estado";
import { useLenguaje } from "@/components/LenguajeContext";
import { api, ApiError } from "@/lib/api";
import { nombreDeDificultad } from "@/lib/format";
import type { ExerciseSummary } from "@/lib/types";

type Bloque = {
  clave: string;
  titulo: string;
  descripcion: string;
  ejercicios: ExerciseSummary[];
};

export default function PaginaRepaso() {
  const { lenguaje, listo } = useLenguaje();
  const [bloques, setBloques] = useState<Bloque[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!listo) {
      return;
    }
    let activo = true;
    setCargando(true);
    setError(null);

    Promise.all([
      api.ejercicios({ language: lenguaje, status: "failed", perPage: 100 }),
      api.ejercicios({ language: lenguaje, status: "pending", perPage: 6 }),
    ])
      .then(([fallados, pendientes]) => {
        if (!activo) {
          return;
        }
        setBloques([
          {
            clave: "fallados",
            titulo: "Ejercicios que se te resistieron",
            descripcion:
              "Ya los intentaste pero todavía no pasan los tests. Son los que más te van a hacer crecer.",
            ejercicios: fallados.data,
          },
          {
            clave: "pendientes",
            titulo: "Siguientes pendientes",
            descripcion: "Ejercicios que aún no has empezado.",
            ejercicios: pendientes.data,
          },
        ]);
      })
      .catch((fallo: unknown) => {
        if (activo) {
          setError(fallo instanceof ApiError ? fallo.message : "No se pudo cargar el repaso.");
        }
      })
      .finally(() => {
        if (activo) {
          setCargando(false);
        }
      });

    return () => {
      activo = false;
    };
  }, [lenguaje, listo]);

  if (cargando) {
    return <EstadoCargando mensaje="Preparando tu repaso…" />;
  }

  if (error) {
    return <Estado tono="error" titulo="No se pudo cargar el repaso" mensaje={error} />;
  }

  const sinPendientes = bloques.every((bloque) => bloque.ejercicios.length === 0);

  return (
    <div className="space-y-8">
      <header>
        <h1 className="text-2xl font-bold text-zinc-900">Repaso</h1>
        <p className="mt-1 text-sm text-zinc-500">
          Refuerza lo que falló y retoma lo que dejaste pendiente.
        </p>
      </header>

      {sinPendientes ? (
        <Estado
          titulo="No hay nada que repasar"
          mensaje="Has resuelto todos los ejercicios del catálogo. ¡Buen trabajo!"
        >
          <Link
            href="/niveles"
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
          >
            Ver ejercicios
          </Link>
        </Estado>
      ) : null}

      {bloques.map((bloque) => (
        <section key={bloque.clave} className="space-y-3">
          <div>
            <h2 className="text-lg font-semibold text-zinc-900">{bloque.titulo}</h2>
            <p className="text-sm text-zinc-500">{bloque.descripcion}</p>
          </div>

          {bloque.ejercicios.length === 0 ? (
            <p className="rounded-lg border border-zinc-200 bg-white px-4 py-6 text-sm text-zinc-500">
              {bloque.clave === "fallados"
                ? "Nada que repasar: no tienes ejercicios fallados pendientes."
                : "Ya empezaste todos los ejercicios del catálogo."}
            </p>
          ) : (
            <ul className="divide-y divide-zinc-100 overflow-hidden rounded-lg border border-zinc-200 bg-white">
              {bloque.ejercicios.map((ejercicio) => (
                <li key={ejercicio.slug}>
                  <Link
                    href={`/ejercicios/${ejercicio.slug}`}
                    className="flex items-center justify-between gap-4 px-4 py-3 transition-colors hover:bg-indigo-50/40"
                  >
                    <div>
                      <p className="font-medium text-zinc-900">{ejercicio.title}</p>
                      <p className="text-xs text-zinc-500">
                        Nivel {ejercicio.level} · {ejercicio.topicName} ·{" "}
                        {nombreDeDificultad(ejercicio.difficulty)}
                      </p>
                    </div>
                    <span className="shrink-0 text-sm text-indigo-600">Practicar →</span>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </section>
      ))}
    </div>
  );
}
