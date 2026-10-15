"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import { nombreDeDificultad } from "@/lib/format";
import type { ExerciseSummary, Level } from "@/lib/types";
import { Estado, EstadoCargando } from "./Estado";
import { useLenguaje } from "./LenguajeContext";

const FILTROS_ESTADO = [
  { valor: "", etiqueta: "Todos" },
  { valor: "pending", etiqueta: "Pendientes" },
  { valor: "solved", etiqueta: "Resueltos" },
  { valor: "failed", etiqueta: "Fallados" },
] as const;

type Props = {
  level: Level;
};

export function ListaEjercicios({ level }: Props) {
  const { lenguaje } = useLenguaje();
  const [ejercicios, setEjercicios] = useState<ExerciseSummary[]>([]);
  const [total, setTotal] = useState(0);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tema, setTema] = useState("");
  const [estado, setEstado] = useState<string>("");
  const [busqueda, setBusqueda] = useState("");

  useEffect(() => {
    let activo = true;
    setCargando(true);
    setError(null);

    api
      .ejercicios({
        language: lenguaje,
        level: level.level,
        topic: tema || undefined,
        status: estado || undefined,
        search: busqueda.trim() || undefined,
        perPage: 100,
      })
      .then((respuesta) => {
        if (!activo) {
          return;
        }
        setEjercicios(respuesta.data);
        setTotal(respuesta.pagination?.total ?? respuesta.data.length);
      })
      .catch((fallo: unknown) => {
        if (activo) {
          setError(fallo instanceof ApiError ? fallo.message : "No se pudieron cargar los ejercicios.");
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
  }, [level.level, lenguaje, tema, estado, busqueda]);

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-end gap-3 rounded-lg border border-zinc-200 bg-white p-4">
        <div>
          <label htmlFor="tema" className="block text-xs font-medium text-zinc-500">
            Tema
          </label>
          <select
            id="tema"
            value={tema}
            onChange={(evento) => setTema(evento.target.value)}
            className="mt-1 rounded-md border border-zinc-300 px-3 py-1.5 text-sm text-zinc-800 focus:border-indigo-500 focus:outline-none"
          >
            <option value="">Todos los temas</option>
            {level.sections.map((seccion) => (
              <option key={seccion.id} value={seccion.id}>
                {seccion.name}
              </option>
            ))}
          </select>
        </div>

        <div className="flex gap-1" role="group" aria-label="Filtrar por estado">
          {FILTROS_ESTADO.map((opcion) => (
            <button
              key={opcion.valor}
              type="button"
              onClick={() => setEstado(opcion.valor)}
              aria-pressed={estado === opcion.valor}
              className={`rounded-md px-3 py-1.5 text-sm font-medium transition-colors ${
                estado === opcion.valor
                  ? "bg-indigo-600 text-white"
                  : "border border-zinc-300 text-zinc-700 hover:bg-zinc-100"
              }`}
            >
              {opcion.etiqueta}
            </button>
          ))}
        </div>

        <div className="min-w-0 flex-1">
          <label htmlFor="busqueda" className="block text-xs font-medium text-zinc-500">
            Buscar
          </label>
          <input
            id="busqueda"
            type="search"
            defaultValue={busqueda}
            onChange={(evento) => setBusqueda(evento.target.value)}
            placeholder="Por título o slug…"
            className="mt-1 w-full rounded-md border border-zinc-300 px-3 py-1.5 text-sm text-zinc-800 focus:border-indigo-500 focus:outline-none"
          />
        </div>

        <p className="text-sm text-zinc-500">
          {total} {total === 1 ? "ejercicio" : "ejercicios"}
        </p>
      </div>

      {cargando ? <EstadoCargando mensaje="Cargando ejercicios…" /> : null}

      {!cargando && error ? (
        <Estado tono="error" titulo="No se pudieron cargar los ejercicios" mensaje={error} />
      ) : null}

      {!cargando && !error && ejercicios.length === 0 ? (
        <Estado
          titulo="No hay ejercicios con esos filtros"
          mensaje="Prueba con otro tema, otro estado o limpia la búsqueda."
        />
      ) : null}

      {level.sections.map((seccion) => {
        const delTema = ejercicios.filter((ejercicio) => ejercicio.topic === seccion.id);
        if (delTema.length === 0) {
          return null;
        }
        return (
          <section key={seccion.id} aria-labelledby={`seccion-${seccion.id}`} className="space-y-3">
            <div>
              <div className="flex items-baseline justify-between gap-3">
                <h2 id={`seccion-${seccion.id}`} className="text-lg font-semibold text-zinc-900">
                  {seccion.name}
                </h2>
                <span className="text-sm text-zinc-500">
                  {seccion.solved} de {seccion.total}
                </span>
              </div>
              <p className="text-sm text-zinc-500">{seccion.description}</p>
            </div>
              <ul className="grid gap-3 sm:grid-cols-2">
                {delTema.map((ejercicio) => (
                  <li key={ejercicio.slug}>
                    <Link
                      href={`/ejercicios/${ejercicio.slug}`}
                      className="flex h-full flex-col justify-between rounded-lg border border-zinc-200 bg-white p-4 transition-colors hover:border-indigo-300 hover:bg-indigo-50/40"
                    >
                      <div>
                        <div className="flex items-start justify-between gap-3">
                          <h3 className="font-semibold text-zinc-900">{ejercicio.title}</h3>
                          {ejercicio.solved ? (
                            <span className="shrink-0 rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-semibold text-emerald-800">
                              Resuelto
                            </span>
                          ) : ejercicio.attempted ? (
                            <span className="shrink-0 rounded-full bg-amber-100 px-2 py-0.5 text-xs font-semibold text-amber-800">
                              En progreso
                            </span>
                          ) : null}
                        </div>
                        <p className="mt-1 text-sm text-zinc-500">
                          {nombreDeDificultad(ejercicio.difficulty)}
                        </p>
                      </div>
                      <p className="mt-3 font-mono text-xs text-zinc-400">
                        {ejercicio.testsCount} tests · {ejercicio.estimatedMinutes ?? "?"} min
                      </p>
                    </Link>
                  </li>
                ))}
              </ul>
          </section>
        );
      })}
    </div>
  );
}
