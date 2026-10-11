"use client";

import { useEffect, useState } from "react";
import { CambiarCorreo } from "@/components/CambiarCorreo";
import { Estado, EstadoCargando } from "@/components/Estado";
import { api, ApiError } from "@/lib/api";
import { formatearFecha, nombreDeEstado } from "@/lib/format";
import type { Attempt, User } from "@/lib/types";

export default function PaginaPerfil() {
  const [usuario, setUsuario] = useState<User | null>(null);
  const [historial, setHistorial] = useState<Attempt[]>([]);
  const [totalIntentos, setTotalIntentos] = useState(0);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let activo = true;

    Promise.all([api.usuarioActual(), api.historial(1, 20)])
      .then(([perfil, intentos]) => {
        if (!activo) {
          return;
        }
        setUsuario(perfil.data);
        setHistorial(intentos.data);
        setTotalIntentos(intentos.pagination?.total ?? intentos.data.length);
      })
      .catch((fallo: unknown) => {
        if (activo) {
          setError(fallo instanceof ApiError ? fallo.message : "No se pudo cargar el perfil.");
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

  if (cargando) {
    return <EstadoCargando mensaje="Cargando tu perfil…" />;
  }

  if (error || !usuario) {
    return (
      <Estado
        tono="error"
        titulo="No se pudo cargar el perfil"
        mensaje={error ?? "Vuelve a iniciar sesión."}
      />
    );
  }

  return (
    <div className="space-y-8">
      <header>
        <h1 className="text-2xl font-bold text-zinc-900">Perfil</h1>
        <p className="mt-1 text-sm text-zinc-500">Tus datos y los últimos envíos.</p>
      </header>

      <section className="rounded-lg border border-zinc-200 bg-white p-5">
        <dl className="grid gap-4 sm:grid-cols-3">
          <div>
            <dt className="text-xs font-medium uppercase tracking-wide text-zinc-500">Nombre</dt>
            <dd className="mt-1 text-sm text-zinc-900">{usuario.displayName}</dd>
          </div>
          <div>
            <dt className="text-xs font-medium uppercase tracking-wide text-zinc-500">Correo</dt>
            <dd className="mt-1 text-sm text-zinc-900">{usuario.email}</dd>
          </div>
          <div>
            <dt className="text-xs font-medium uppercase tracking-wide text-zinc-500">Miembro desde</dt>
            <dd className="mt-1 text-sm text-zinc-900">{formatearFecha(usuario.createdAt)}</dd>
          </div>
        </dl>
        <div className="mt-4">
          <CambiarCorreo correoActual={usuario.email} onCambiado={setUsuario} />
        </div>
      </section>

      <section className="space-y-3">
        <div className="flex items-baseline justify-between">
          <h2 className="text-lg font-semibold text-zinc-900">Historial de envíos</h2>
          <span className="text-sm text-zinc-500">{totalIntentos} en total</span>
        </div>

        {historial.length === 0 ? (
          <Estado
            titulo="Todavía no has enviado ninguna solución"
            mensaje="Cuando envíes tu primer ejercicio, aquí verás el historial."
          />
        ) : (
          <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white">
            <table className="w-full text-left text-sm">
              <thead className="bg-zinc-50 text-xs uppercase tracking-wide text-zinc-500">
                <tr>
                  <th scope="col" className="px-4 py-2 font-medium">Ejercicio</th>
                  <th scope="col" className="px-4 py-2 font-medium">Resultado</th>
                  <th scope="col" className="px-4 py-2 font-medium">Tests</th>
                  <th scope="col" className="px-4 py-2 font-medium">Fecha</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-zinc-100">
                {historial.map((intento, indice) => (
                  <tr key={`${intento.exerciseSlug}-${intento.createdAt}-${indice}`}>
                    <td className="px-4 py-2 text-zinc-900">{intento.exerciseTitle}</td>
                    <td className="px-4 py-2">
                      <span
                        className={`rounded-full px-2 py-0.5 text-xs font-semibold ${
                          intento.passed
                            ? "bg-emerald-100 text-emerald-800"
                            : "bg-amber-100 text-amber-800"
                        }`}
                      >
                        {nombreDeEstado(intento.status)}
                      </span>
                    </td>
                    <td className="px-4 py-2 text-zinc-600">
                      {intento.testsPassed} / {intento.testsTotal}
                    </td>
                    <td className="px-4 py-2 text-zinc-500">{formatearFecha(intento.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
