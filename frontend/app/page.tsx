"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { BarraProgreso } from "@/components/BarraProgreso";
import { Estado, EstadoCargando } from "@/components/Estado";
import { nombreDeLenguaje, useLenguaje } from "@/components/LenguajeContext";
import { SelectorLenguaje } from "@/components/SelectorLenguaje";
import { api, ApiError, nombreDeNivel } from "@/lib/api";
import type { ExerciseSummary, ProgressSummary } from "@/lib/types";

export default function PaginaInicio() {
  const { lenguaje, listo } = useLenguaje();
  const [progreso, setProgreso] = useState<ProgressSummary | null>(null);
  const [siguiente, setSiguiente] = useState<ExerciseSummary | null>(null);
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
      api.progreso(lenguaje),
      api.ejercicios({ language: lenguaje, status: "pending", perPage: 1 }),
    ])
      .then(([resumen, pendientes]) => {
        if (!activo) {
          return;
        }
        setProgreso(resumen.data);
        setSiguiente(pendientes.data[0] ?? null);
      })
      .catch((fallo: unknown) => {
        if (activo) {
          setError(fallo instanceof ApiError ? fallo.message : "No se pudo cargar tu progreso.");
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
    return <EstadoCargando mensaje="Cargando tu progreso…" />;
  }

  if (error || !progreso) {
    return (
      <Estado
        tono="error"
        titulo="No se pudo cargar tu progreso"
        mensaje={error ?? "Intenta recargar la página."}
      />
    );
  }

  const porcentajeTotal =
    progreso.total === 0 ? 0 : Math.round((progreso.solved / progreso.total) * 100);

  if (progreso.attempts === 0) {
    return (
      <div className="space-y-6">
        <header className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold text-zinc-900">Bienvenido al Java Practice Lab</h1>
            <p className="mt-2 max-w-2xl text-zinc-600">
              {progreso.total} ejercicios de {nombreDeLenguaje(lenguaje)} para entrenar entrevistas
              técnicas: escribes el código en el navegador, se compila y se ejecutan los tests por ti.
            </p>
          </div>
          <SelectorLenguaje />
        </header>

        <div className="rounded-lg border border-indigo-200 bg-indigo-50 p-6">
          <h2 className="font-semibold text-indigo-900">Empieza por el primer ejercicio</h2>
          <p className="mt-1 text-sm text-indigo-800">
            Cada ejercicio trae enunciado, pistas y tests automáticos. No hace falta configurar nada.
          </p>
          <Link
            href={siguiente ? `/ejercicios/${siguiente.slug}` : "/niveles"}
            className="mt-4 inline-block rounded-md bg-indigo-600 px-5 py-2 text-sm font-semibold text-white hover:bg-indigo-700"
          >
            {siguiente ? `Resolver «${siguiente.title}»` : "Ver los ejercicios"}
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <header className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-zinc-900">Tu progreso en {nombreDeLenguaje(lenguaje)}</h1>
          <p className="mt-1 text-sm text-zinc-500">
            {progreso.solved} de {progreso.total} ejercicios resueltos ({porcentajeTotal}%)
          </p>
          <BarraProgreso valor={porcentajeTotal} etiqueta="Avance general" className="mt-3 max-w-xl" />
        </div>
        <SelectorLenguaje />
      </header>

      <section aria-label="Resumen" className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Tarjeta etiqueta="Puntos de experiencia" valor={`${progreso.xp} XP`} />
        <Tarjeta etiqueta="Ejercicios resueltos" valor={`${progreso.solved} / ${progreso.total}`} />
        <Tarjeta
          etiqueta="Racha"
          valor={`${progreso.streakDays} ${progreso.streakDays === 1 ? "día" : "días"}`}
        />
        <Tarjeta etiqueta="Intentos enviados" valor={String(progreso.attempts)} />
      </section>

      {siguiente ? (
        <section className="rounded-lg border border-indigo-200 bg-indigo-50 p-6">
          <h2 className="font-semibold text-indigo-900">Continúa donde lo dejaste</h2>
          <p className="mt-1 text-sm text-indigo-800">
            {siguiente.title} · {siguiente.topicName}
          </p>
          <Link
            href={`/ejercicios/${siguiente.slug}`}
            className="mt-4 inline-block rounded-md bg-indigo-600 px-5 py-2 text-sm font-semibold text-white hover:bg-indigo-700"
          >
            Seguir practicando
          </Link>
        </section>
      ) : null}

      <section className="space-y-4">
        <div className="flex items-baseline justify-between">
          <h2 className="text-lg font-semibold text-zinc-900">Avance por nivel</h2>
          <Link href="/niveles" className="text-sm text-indigo-600 hover:underline">
            Ver todos los ejercicios
          </Link>
        </div>

        <ul className="space-y-4">
          {progreso.levels.map((nivel) => (
            <li key={nivel.level}>
              <Link
                href={`/niveles/${nivel.level}`}
                className="block rounded-lg border border-zinc-200 bg-white p-4 transition-colors hover:border-indigo-300 hover:bg-indigo-50/40"
              >
                <div className="flex items-baseline justify-between">
                  <span className="font-medium text-zinc-900">{nombreDeNivel(nivel.level)}</span>
                  <span className="text-sm text-zinc-500">
                    {nivel.solved} de {nivel.total}
                  </span>
                </div>
                <BarraProgreso valor={nivel.percent} etiqueta="Avance del nivel" className="mt-3" />
              </Link>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}

function Tarjeta({ etiqueta, valor }: { etiqueta: string; valor: string }) {
  return (
    <div className="rounded-lg border border-zinc-200 bg-white p-4">
      <p className="text-xs font-medium uppercase tracking-wide text-zinc-500">{etiqueta}</p>
      <p className="mt-1 text-2xl font-bold text-zinc-900">{valor}</p>
    </div>
  );
}
