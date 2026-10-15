"use client";

import { LENGUAJES, useLenguaje } from "./LenguajeContext";

/** Pestañas para elegir el lenguaje. Filtra todo el laboratorio (niveles, ejercicios, dashboard). */
export function SelectorLenguaje({ className = "" }: { className?: string }) {
  const { lenguaje, cambiarLenguaje } = useLenguaje();

  return (
    <div
      role="tablist"
      aria-label="Lenguaje"
      className={`inline-flex rounded-lg border border-zinc-200 bg-white p-1 ${className}`}
    >
      {LENGUAJES.map((opcion) => {
        const activo = opcion.id === lenguaje;
        return (
          <button
            key={opcion.id}
            role="tab"
            type="button"
            aria-selected={activo}
            onClick={() => cambiarLenguaje(opcion.id)}
            className={`rounded-md px-4 py-1.5 text-sm font-medium transition-colors ${
              activo
                ? "bg-indigo-600 text-white"
                : "text-zinc-600 hover:bg-zinc-100 hover:text-zinc-900"
            }`}
          >
            {opcion.nombre}
          </button>
        );
      })}
    </div>
  );
}
