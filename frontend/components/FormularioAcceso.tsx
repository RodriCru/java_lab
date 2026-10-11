"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { api, ApiError } from "@/lib/api";

type Props = {
  modo: "login" | "registro";
  destino?: string;
};

export function FormularioAcceso({ modo, destino }: Props) {
  const router = useRouter();
  const esRegistro = modo === "registro";

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function manejarEnvio(evento: React.FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setError(null);
    setEnviando(true);

    try {
      if (esRegistro) {
        await api.registrar({
          email,
          password,
          displayName: displayName.trim() || undefined,
        });
      } else {
        await api.iniciarSesion({ email, password });
      }
      router.push(destino && destino.startsWith("/") ? destino : "/");
      router.refresh();
    } catch (fallo: unknown) {
      setError(fallo instanceof ApiError ? fallo.message : "No se pudo completar la operación.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="mx-auto w-full max-w-md">
      <div className="rounded-xl border border-zinc-200 bg-white p-8">
        <h1 className="text-2xl font-bold text-zinc-900">
          {esRegistro ? "Crea tu cuenta" : "Inicia sesión"}
        </h1>
        <p className="mt-2 text-sm text-zinc-500">
          {esRegistro
            ? "Tu progreso se guarda por cuenta, así que puedes seguir donde lo dejaste."
            : "Retoma tus ejercicios pendientes y sigue practicando para tu entrevista."}
        </p>

        <form onSubmit={manejarEnvio} className="mt-6 space-y-4">
          {esRegistro ? (
            <div>
              <label htmlFor="displayName" className="block text-sm font-medium text-zinc-700">
                Nombre <span className="font-normal text-zinc-400">(opcional)</span>
              </label>
              <input
                id="displayName"
                type="text"
                autoComplete="name"
                value={displayName}
                onChange={(evento) => setDisplayName(evento.target.value)}
                className="mt-1 w-full rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-indigo-500 focus:outline-none"
              />
            </div>
          ) : null}

          <div>
            <label htmlFor="email" className="block text-sm font-medium text-zinc-700">
              Correo
            </label>
            <input
              id="email"
              type="email"
              required
              autoComplete="email"
              value={email}
              onChange={(evento) => setEmail(evento.target.value)}
              className="mt-1 w-full rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-indigo-500 focus:outline-none"
            />
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-medium text-zinc-700">
              Contraseña
            </label>
            <input
              id="password"
              type="password"
              required
              minLength={8}
              autoComplete={esRegistro ? "new-password" : "current-password"}
              value={password}
              onChange={(evento) => setPassword(evento.target.value)}
              className="mt-1 w-full rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-indigo-500 focus:outline-none"
            />
            {esRegistro ? (
              <p className="mt-1 text-xs text-zinc-500">Mínimo 8 caracteres.</p>
            ) : (
              <p className="mt-1 text-right text-xs">
                <Link href="/olvide-contrasena" className="font-medium text-indigo-600 hover:underline">
                  ¿Olvidaste tu contraseña?
                </Link>
              </p>
            )}
          </div>

          {error ? (
            <p role="alert" className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {error}
            </p>
          ) : null}

          <button
            type="submit"
            disabled={enviando}
            className="w-full rounded-md bg-indigo-600 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {enviando ? "Un momento…" : esRegistro ? "Crear cuenta" : "Entrar"}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-zinc-600">
          {esRegistro ? "¿Ya tienes cuenta? " : "¿Aún no tienes cuenta? "}
          <Link href={esRegistro ? "/login" : "/registro"} className="font-medium text-indigo-600 hover:underline">
            {esRegistro ? "Inicia sesión" : "Regístrate"}
          </Link>
        </p>
      </div>
    </div>
  );
}
