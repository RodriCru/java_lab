"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { CampoCodigo } from "@/components/CampoCodigo";
import { api, ApiError } from "@/lib/api";

const ESPERA_REENVIO_SEGUNDOS = 60;
const CLASE_INPUT =
  "mt-1 w-full rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-indigo-500 focus:outline-none";

type Paso = "correo" | "codigo" | "listo";

export function RecuperarContrasena() {
  const [paso, setPaso] = useState<Paso>("correo");
  const [email, setEmail] = useState("");
  const [codigo, setCodigo] = useState("");
  const [password, setPassword] = useState("");
  const [confirmacion, setConfirmacion] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [espera, setEspera] = useState(0);

  useEffect(() => {
    if (espera <= 0) {
      return;
    }
    const temporizador = setTimeout(() => setEspera((segundos) => segundos - 1), 1000);
    return () => clearTimeout(temporizador);
  }, [espera]);

  function mensajeDe(fallo: unknown): string {
    return fallo instanceof ApiError ? fallo.message : "No se pudo completar la operación.";
  }

  async function pedirCodigo(evento?: React.FormEvent<HTMLFormElement>) {
    evento?.preventDefault();
    setError(null);
    setAviso(null);
    setEnviando(true);
    try {
      const respuesta = await api.olvideContrasena(email.trim());
      setAviso(respuesta.message ?? null);
      setCodigo("");
      setEspera(ESPERA_REENVIO_SEGUNDOS);
      setPaso("codigo");
    } catch (fallo: unknown) {
      setError(mensajeDe(fallo));
    } finally {
      setEnviando(false);
    }
  }

  async function restablecer(evento: React.FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setError(null);

    if (codigo.length !== 6) {
      setError("Escribe los 6 dígitos del código.");
      return;
    }
    if (password !== confirmacion) {
      setError("Las contraseñas no coinciden.");
      return;
    }

    setEnviando(true);
    try {
      await api.restablecerContrasena({ email: email.trim(), code: codigo, newPassword: password });
      setPaso("listo");
    } catch (fallo: unknown) {
      setError(mensajeDe(fallo));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="mx-auto w-full max-w-md">
      <div className="rounded-xl border border-zinc-200 bg-white p-8">
        {paso === "correo" ? (
          <>
            <h1 className="text-2xl font-bold text-zinc-900">Recupera tu contraseña</h1>
            <p className="mt-2 text-sm text-zinc-500">
              Escribe el correo de tu cuenta y te enviamos un código de 6 dígitos.
            </p>
            <form onSubmit={pedirCodigo} className="mt-6 space-y-4">
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
                  className={CLASE_INPUT}
                />
              </div>
              {error ? <Alerta texto={error} /> : null}
              <button type="submit" disabled={enviando} className={CLASE_BOTON}>
                {enviando ? "Enviando…" : "Enviar código"}
              </button>
            </form>
          </>
        ) : null}

        {paso === "codigo" ? (
          <>
            <h1 className="text-2xl font-bold text-zinc-900">Escribe tu código</h1>
            <p className="mt-2 text-sm text-zinc-500">
              {aviso ?? "Te enviamos un código de 6 dígitos."} Vence en 10 minutos.
            </p>
            <form onSubmit={restablecer} className="mt-6 space-y-4">
              <CampoCodigo valor={codigo} onCambio={setCodigo} disabled={enviando} />

              <div>
                <label htmlFor="password" className="block text-sm font-medium text-zinc-700">
                  Contraseña nueva
                </label>
                <input
                  id="password"
                  type="password"
                  required
                  minLength={8}
                  maxLength={72}
                  autoComplete="new-password"
                  value={password}
                  onChange={(evento) => setPassword(evento.target.value)}
                  className={CLASE_INPUT}
                />
                <p className="mt-1 text-xs text-zinc-500">Mínimo 8 caracteres.</p>
              </div>
              <div>
                <label htmlFor="confirmacion" className="block text-sm font-medium text-zinc-700">
                  Repite la contraseña
                </label>
                <input
                  id="confirmacion"
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

              {error ? <Alerta texto={error} /> : null}
              <button type="submit" disabled={enviando} className={CLASE_BOTON}>
                {enviando ? "Un momento…" : "Cambiar contraseña"}
              </button>
            </form>

            <div className="mt-4 flex items-center justify-between text-sm">
              <button
                type="button"
                onClick={() => setPaso("correo")}
                className="text-zinc-600 hover:underline"
              >
                Cambiar correo
              </button>
              <button
                type="button"
                disabled={espera > 0 || enviando}
                onClick={() => void pedirCodigo()}
                className="font-medium text-indigo-600 hover:underline disabled:cursor-not-allowed disabled:text-zinc-400 disabled:no-underline"
              >
                {espera > 0 ? `Reenviar en ${espera} s` : "Reenviar código"}
              </button>
            </div>
          </>
        ) : null}

        {paso === "listo" ? (
          <div className="text-center">
            <h1 className="text-2xl font-bold text-zinc-900">Contraseña actualizada</h1>
            <p className="mt-2 text-sm text-zinc-500">Ya puedes iniciar sesión con tu contraseña nueva.</p>
            <Link href="/login" className={`${CLASE_BOTON} mt-6 inline-block`}>
              Ir a iniciar sesión
            </Link>
          </div>
        ) : null}

        {paso !== "listo" ? (
          <p className="mt-6 text-center text-sm text-zinc-600">
            <Link href="/login" className="font-medium text-indigo-600 hover:underline">
              Volver a iniciar sesión
            </Link>
          </p>
        ) : null}
      </div>
    </div>
  );
}

const CLASE_BOTON =
  "w-full rounded-md bg-indigo-600 px-4 py-2 text-center text-sm font-semibold text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60";

function Alerta({ texto }: { texto: string }) {
  return (
    <p role="alert" className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
      {texto}
    </p>
  );
}
