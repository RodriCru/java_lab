"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { BarraProgreso } from "@/components/BarraProgreso";
import { Estado, EstadoCargando } from "@/components/Estado";
import { api, ApiError } from "@/lib/api";
import type { Level } from "@/lib/types";

export default function PaginaNiveles() {
  const [niveles, setNiveles] = useState<Level[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let activo = true;

    api
      .niveles()
      .then((respuesta) => {
        if (activo) {
          setNiveles(respuesta.data);
        }
      })
      .catch((fallo: unknown) => {
        if (activo) {
          setError(fallo instanceof ApiError ? fallo.message : "No se pudieron cargar los niveles.");
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
  }, []);

  const totalEjercicios = niveles.reduce((suma, nivel) => suma + nivel.total, 0);
  const totalResueltos = niveles.reduce((suma, nivel) => suma + nivel.solved, 0);
  const porcentajeGeneral =
    totalEjercicios === 0 ? 0 : Math.round((totalResueltos / totalEjercicios) * 100);

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-2xl font-bold text-zinc-900">Ejercicios</h1>
        <p className="mt-1 text-sm text-zinc-500">
          {totalEjercicios} ejercicios en {niveles.length}{" "}
          {niveles.length === 1 ? "nivel" : "niveles"} · llevas {totalResueltos} resueltos (
          {porcentajeGeneral}%)
        </p>
      </header>

      {cargando ? <EstadoCargando mensaje="Cargando niveles…" /> : null}

      {!cargando && error ? (
        <Estado tono="error" titulo="No se pudieron cargar los niveles" mensaje={error} />
      ) : null}

      {!cargando && !error && niveles.length === 0 ? (
        <Estado
          titulo="Todavía no hay niveles"
          mensaje="El catálogo de ejercicios está vacío. Revisa el directorio 'exercises' del proyecto."
        />
      ) : null}

      <ul className="grid gap-4 sm:grid-cols-2">
        {niveles.map((nivel) => (
          <li key={nivel.level}>
            <Link
              href={`/niveles/${nivel.level}`}
              className="block rounded-lg border border-zinc-200 bg-white p-5 transition-colors hover:border-indigo-300 hover:bg-indigo-50/40"
            >
              <div className="flex items-baseline justify-between">
                <h2 className="text-lg font-semibold text-zinc-900">{nivel.name}</h2>
                <span className="text-sm text-zinc-500">
                  {nivel.solved} de {nivel.total}
                </span>
              </div>
              <p className="mt-1 text-sm text-zinc-500">
                {nivel.topics.length} {nivel.topics.length === 1 ? "tema" : "temas"} ·{" "}
                {nivel.total} {nivel.total === 1 ? "ejercicio" : "ejercicios"}
              </p>
              <BarraProgreso valor={nivel.percent} etiqueta="Avance" className="mt-4" />
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
