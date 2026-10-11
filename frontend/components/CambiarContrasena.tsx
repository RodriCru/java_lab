"use client";

import { useState } from "react";
import { api, ApiError } from "@/lib/api";

const CLASE_INPUT =
  "mt-1 w-full rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-indigo-500 focus:outline-none";

/** Cambio de contrasena con la sesion abierta (por ejemplo, para sustituir la temporal de una invitacion). */
export function CambiarContrasena() {
  const [abierto, setAbierto] = useState(false);
  const [actual, setActual] = useState("");
  const [nueva, setNueva] = useState("");
  const [confirmacion, setConfirmacion] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [listo, setListo] = useState(false);
  const [enviando, setEnviando] = useState(false);

  function cerrar() {
    setAbierto(false);
    setActual("");
    setNueva("");
    setConfirmacion("");
    setError(null);
  }

  async function enviar(evento: React.FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setError(null);
    if (nueva !== confirmacion) {
      setError("Las contraseñas nuevas no coinciden.");
      return;
    }
    setEnviando(true);
    try {
      await api.cambiarContrasena({ currentPassword: actual, newPassword: nueva });
      cerrar();
      setListo(true);
    } catch (fallo: unknown) {
      setError(fallo instanceof ApiError ? fallo.message : "No se pudo completar la operación.");
    } finally {
      setEnviando(false);
    }
  }

  if (!abierto) {
    return (
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={() => {
            setListo(false);
            setAbierto(true);
          }}
          className="text-xs font-medium text-indigo-600 hover:underline"
        >
          Cambiar contraseña
        </button>
        {listo ? (
          <span role="status" className="text-xs font-medium text-emerald-700">
            Contraseña actualizada.
          </span>
        ) : null}
      </div>
    );
  }

  return (
    <div className="rounded-lg border border-zinc-200 bg-zinc-50 p-4">
      <h3 className="text-sm font-semibold text-zinc-900">Cambiar contraseña</h3>
      <form onSubmit={enviar} className="mt-3 space-y-3">
        <div>
          <label htmlFor="pw-actual" className="block text-sm font-medium text-zinc-700">
            Contraseña actual
          </label>
          <input
            id="pw-actual"
            type="password"
            required
            autoComplete="current-password"
            value={actual}
            onChange={(evento) => setActual(evento.target.value)}
            className={CLASE_INPUT}
          />
        </div>
        <div>
          <label htmlFor="pw-nueva" className="block text-sm font-medium text-zinc-700">
            Contraseña nueva
          </label>
          <input
            id="pw-nueva"
            type="password"
            required
            minLength={8}
            maxLength={72}
            autoComplete="new-password"
            value={nueva}
            onChange={(evento) => setNueva(evento.target.value)}
            className={CLASE_INPUT}
          />
          <p className="mt-1 text-xs text-zinc-500">Mínimo 8 caracteres.</p>
        </div>
        <div>
          <label htmlFor="pw-confirmacion" className="block text-sm font-medium text-zinc-700">
            Repite la contraseña nueva
          </label>
          <input
            id="pw-confirmacion"
            type="password"
            required
            minLength={8}
            maxLength={72}
            autoComplete="new-password"
            value={confirmacion}
            onChange={(evento) => setConfirmacion(evento.target.value)}
            className={CLASE_INPUT}
          />
        </div>
        {error ? (
          <p role="alert" className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}
          </p>
        ) : null}
        <div className="flex gap-2">
          <button
            type="submit"
            disabled={enviando}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {enviando ? "Guardando…" : "Guardar"}
          </button>
          <button type="button" onClick={cerrar} className="px-3 py-2 text-sm text-zinc-600 hover:underline">
            Cancelar
          </button>
        </div>
      </form>
    </div>
  );
}
