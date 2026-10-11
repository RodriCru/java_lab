"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { BarraProgreso } from "@/components/BarraProgreso";
import { Estado, EstadoCargando } from "@/components/Estado";
import { ListaEjercicios } from "@/components/ListaEjercicios";
import { api, ApiError } from "@/lib/api";
import type { Level } from "@/lib/types";

export function VistaNivel({ level }: { level: number }) {
  const [nivel, setNivel] = useState<Level | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let activo = true;
    setCargando(true);
    setError(null);

    api
      .niveles()
      .then((respuesta) => {
        if (!activo) {
          return;
        }
        const encontrado = respuesta.data.find((item) => item.level === level) ?? null;
        setNivel(encontrado);
        if (!encontrado) {
          setError(`No existe el nivel ${level}.`);
        }
      })
      .catch((fallo: unknown) => {
        if (activo) {
          setError(fallo instanceof ApiError ? fallo.message : "No se pudo cargar el nivel.");
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
  }, [level]);

  if (cargando) {
    return <EstadoCargando mensaje="Cargando nivel…" />;
  }

  if (error || !nivel) {
    return (
      <Estado tono="error" titulo="No se pudo abrir el nivel" mensaje={error ?? "Nivel desconocido."}>
        <Link
          href="/niveles"
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
        >
          Ver todos los niveles
        </Link>
      </Estado>
    );
  }

  return (
    <div className="space-y-6">
      <header>
        <Link href="/niveles" className="text-sm text-indigo-600 hover:underline">
          ← Todos los niveles
        </Link>
        <h1 className="mt-2 text-2xl font-bold text-zinc-900">{nivel.name}</h1>
        <p className="mt-1 text-sm text-zinc-500">
          {nivel.solved} de {nivel.total} ejercicios resueltos
        </p>
        <BarraProgreso valor={nivel.percent} etiqueta="Avance del nivel" className="mt-3 max-w-sm" />
      </header>

      <ListaEjercicios level={nivel} />
    </div>
  );
}
