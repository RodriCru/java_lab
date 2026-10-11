"use client";

import { useState } from "react";
import { CampoCodigo } from "@/components/CampoCodigo";
import { api, ApiError } from "@/lib/api";
import type { User } from "@/lib/types";

const CLASE_INPUT =
  "mt-1 w-full rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-indigo-500 focus:outline-none";
const CLASE_BOTON =
  "rounded-md bg-indigo-600 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60";

type Props = {
  correoActual: string;
  onCambiado: (usuario: User) => void;
};

/** Cambio de correo en dos pasos: el codigo viaja al correo NUEVO, asi se comprueba que es del usuario. */
export function CambiarCorreo({ correoActual, onCambiado }: Props) {
  const [abierto, setAbierto] = useState(false);
  const [paso, setPaso] = useState<"datos" | "codigo">("datos");
  const [nuevoCorreo, setNuevoCorreo] = useState("");
  const [password, setPassword] = useState("");
  const [codigo, setCodigo] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  function cerrar() {
    setAbierto(false);
    setPaso("datos");
    setNuevoCorreo("");
    setPassword("");
    setCodigo("");
    setError(null);
  }

  function mensajeDe(fallo: unknown): string {
    return fallo instanceof ApiError ? fallo.message : "No se pudo completar la operación.";
  }

  async function pedirCodigo(evento: React.FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setError(null);
    setEnviando(true);
    try {
      await api.pedirCambioDeCorreo({ newEmail: nuevoCorreo.trim(), currentPassword: password });
      setPassword("");
      setPaso("codigo");
    } catch (fallo: unknown) {
      setError(mensajeDe(fallo));
    } finally {
      setEnviando(false);
    }
  }

  async function confirmar(evento: React.FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setError(null);
    if (codigo.length !== 6) {
      setError("Escribe los 6 dígitos del código.");
      return;
    }
    setEnviando(true);
    try {
      const respuesta = await api.confirmarCambioDeCorreo(codigo);
      onCambiado(respuesta.data);
      cerrar();
    } catch (fallo: unknown) {
      setError(mensajeDe(fallo));
    } finally {
      setEnviando(false);
    }
  }

  if (!abierto) {
    return (
      <button
        type="button"
        onClick={() => setAbierto(true)}
        className="text-xs font-medium text-indigo-600 hover:underline"
      >
        Cambiar correo
      </button>
    );
  }

  return (
    <div className="rounded-lg border border-zinc-200 bg-zinc-50 p-4">
      <h3 className="text-sm font-semibold text-zinc-900">Cambiar correo</h3>
      <p className="mt-1 text-xs text-zinc-500">
        Hoy tu correo es <span className="font-medium text-zinc-700">{correoActual}</span>. Enviamos un código al
        correo nuevo para confirmar que es tuyo.
      </p>

      {paso === "datos" ? (
        <form onSubmit={pedirCodigo} className="mt-3 space-y-3">
          <div>
            <label htmlFor="nuevoCorreo" className="block text-sm font-medium text-zinc-700">
              Correo nuevo
            </label>
            <input
              id="nuevoCorreo"
              type="email"
              required
              autoComplete="email"
              value={nuevoCorreo}
              onChange={(evento) => setNuevoCorreo(evento.target.value)}
              className={CLASE_INPUT}
            />
          </div>
          <div>
            <label htmlFor="passwordActual" className="block text-sm font-medium text-zinc-700">
              Contraseña actual
            </label>
            <input
              id="passwordActual"
              type="password"
              required
              autoComplete="current-password"
              value={password}
              onChange={(evento) => setPassword(evento.target.value)}
              className={CLASE_INPUT}
            />
          </div>
          {error ? <Alerta texto={error} /> : null}
          <div className="flex gap-2">
            <button type="submit" disabled={enviando} className={CLASE_BOTON}>
              {enviando ? "Enviando…" : "Enviar código"}
            </button>
            <button type="button" onClick={cerrar} className="px-3 py-2 text-sm text-zinc-600 hover:underline">
              Cancelar
            </button>
          </div>
        </form>
      ) : (
        <form onSubmit={confirmar} className="mt-3 space-y-3">
          <p className="text-sm text-zinc-600">
            Escribe el código que llegó a <span className="font-medium text-zinc-900">{nuevoCorreo}</span>.
          </p>
          <CampoCodigo valor={codigo} onCambio={setCodigo} disabled={enviando} />
          {error ? <Alerta texto={error} /> : null}
          <div className="flex gap-2">
            <button type="submit" disabled={enviando} className={CLASE_BOTON}>
              {enviando ? "Un momento…" : "Confirmar"}
            </button>
            <button type="button" onClick={cerrar} className="px-3 py-2 text-sm text-zinc-600 hover:underline">
              Cancelar
            </button>
          </div>
        </form>
      )}
    </div>
  );
}

function Alerta({ texto }: { texto: string }) {
  return (
    <p role="alert" className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
      {texto}
    </p>
  );
}
