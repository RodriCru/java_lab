"use client";

import { useCallback, useEffect, useState } from "react";
import { Estado, EstadoCargando } from "@/components/Estado";
import { api, ApiError, nombreDeNivel } from "@/lib/api";
import { formatearFecha } from "@/lib/format";
import type { AdminUser } from "@/lib/types";

const CLASE_INPUT =
  "mt-1 w-full rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-900 focus:border-indigo-500 focus:outline-none";

function mensajeDe(fallo: unknown): string {
  return fallo instanceof ApiError ? fallo.message : "No se pudo completar la operación.";
}

/** Panel minimo del SUPER_ADMIN: invitar usuarios y activarlos o desactivarlos. El backend vuelve a comprobar el rol. */
export function PanelAdmin() {
  const [usuarios, setUsuarios] = useState<AdminUser[]>([]);
  const [cargando, setCargando] = useState(true);
  const [sinAcceso, setSinAcceso] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  const [email, setEmail] = useState("");
  const [nombre, setNombre] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [ocupado, setOcupado] = useState<number | null>(null);

  const cargar = useCallback(async () => {
    try {
      const respuesta = await api.adminUsuarios();
      setUsuarios(respuesta.data);
    } catch (fallo: unknown) {
      if (fallo instanceof ApiError && fallo.status === 403) {
        setSinAcceso(true);
      } else {
        setError(mensajeDe(fallo));
      }
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  async function invitar(evento: React.FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setError(null);
    setAviso(null);
    setEnviando(true);
    try {
      const respuesta = await api.adminInvitar({
        email: email.trim(),
        displayName: nombre.trim() || undefined,
      });
      setAviso(respuesta.message ?? "Invitación enviada.");
      setEmail("");
      setNombre("");
      await cargar();
    } catch (fallo: unknown) {
      setError(mensajeDe(fallo));
    } finally {
      setEnviando(false);
    }
  }

  async function reenviar(usuario: AdminUser) {
    if (!window.confirm(`Se generará otra contraseña temporal para ${usuario.email} y la anterior dejará de servir. ¿Continuar?`)) {
      return;
    }
    await ejecutar(usuario.id, () => api.adminReenviar(usuario.id));
  }

  async function cambiarEstado(usuario: AdminUser) {
    await ejecutar(usuario.id, () => api.adminCambiarEstado(usuario.id, !usuario.active));
  }

  async function ejecutar(id: number, accion: () => Promise<{ message?: string }>) {
    setError(null);
    setAviso(null);
    setOcupado(id);
    try {
      const respuesta = await accion();
      setAviso(respuesta.message ?? "Listo.");
      await cargar();
    } catch (fallo: unknown) {
      setError(mensajeDe(fallo));
    } finally {
      setOcupado(null);
    }
  }

  if (cargando) {
    return <EstadoCargando mensaje="Cargando el panel…" />;
  }

  if (sinAcceso) {
    return <Estado tono="error" titulo="Sin acceso" mensaje="Esta sección es solo para el super administrador." />;
  }

  return (
    <div className="space-y-8">
      <header>
        <h1 className="text-2xl font-bold text-zinc-900">Administración</h1>
        <p className="mt-1 text-sm text-zinc-500">
          Las cuentas se crean por invitación: al invitar, la persona recibe un correo con una contraseña temporal
          que puede cambiar desde su perfil.
        </p>
      </header>

      <section className="rounded-lg border border-zinc-200 bg-white p-5">
        <h2 className="text-lg font-semibold text-zinc-900">Invitar a alguien</h2>
        <form onSubmit={invitar} className="mt-4 grid gap-4 sm:grid-cols-[1fr_1fr_auto] sm:items-end">
          <div>
            <label htmlFor="inv-email" className="block text-sm font-medium text-zinc-700">
              Correo
            </label>
            <input
              id="inv-email"
              type="email"
              required
              value={email}
              onChange={(evento) => setEmail(evento.target.value)}
              className={CLASE_INPUT}
            />
          </div>
          <div>
            <label htmlFor="inv-nombre" className="block text-sm font-medium text-zinc-700">
              Nombre <span className="font-normal text-zinc-400">(opcional)</span>
            </label>
            <input
              id="inv-nombre"
              type="text"
              maxLength={60}
              value={nombre}
              onChange={(evento) => setNombre(evento.target.value)}
              className={CLASE_INPUT}
            />
          </div>
          <button
            type="submit"
            disabled={enviando}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {enviando ? "Enviando…" : "Enviar invitación"}
          </button>
        </form>
        {aviso ? (
          <p role="status" className="mt-4 rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-800">
            {aviso}
          </p>
        ) : null}
        {error ? (
          <p role="alert" className="mt-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}
          </p>
        ) : null}
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-semibold text-zinc-900">Usuarios ({usuarios.length})</h2>
        <div className="overflow-x-auto rounded-lg border border-zinc-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="bg-zinc-50 text-xs uppercase tracking-wide text-zinc-500">
              <tr>
                <th scope="col" className="px-4 py-2 font-medium">Correo</th>
                <th scope="col" className="px-4 py-2 font-medium">Nombre</th>
                <th scope="col" className="px-4 py-2 font-medium">Rol</th>
                <th scope="col" className="px-4 py-2 font-medium">Estado</th>
                <th scope="col" className="px-4 py-2 font-medium">Avance por nivel</th>
                <th scope="col" className="px-4 py-2 font-medium">Último acceso</th>
                <th scope="col" className="px-4 py-2 font-medium">
                  <span className="sr-only">Acciones</span>
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {usuarios.map((usuario) => (
                <tr key={usuario.id}>
                  <td className="px-4 py-2 text-zinc-900">{usuario.email}</td>
                  <td className="px-4 py-2 text-zinc-600">{usuario.displayName}</td>
                  <td className="px-4 py-2">
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-semibold ${
                        usuario.role === "SUPER_ADMIN" ? "bg-indigo-100 text-indigo-800" : "bg-zinc-100 text-zinc-700"
                      }`}
                    >
                      {usuario.role === "SUPER_ADMIN" ? "Super admin" : "Usuario"}
                    </span>
                  </td>
                  <td className="px-4 py-2">
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-semibold ${
                        usuario.active ? "bg-emerald-100 text-emerald-800" : "bg-amber-100 text-amber-800"
                      }`}
                    >
                      {usuario.active ? "Activo" : "Desactivado"}
                    </span>
                  </td>
                  <td className="px-4 py-2 text-zinc-600">
                    {usuario.levels?.length ? (
                      <ul className="space-y-0.5 text-xs">
                        {usuario.levels.map((nivel) => (
                          <li key={nivel.level}>
                            {nombreDeNivel(nivel.level)}:{" "}
                            <span className="font-semibold text-zinc-900">{nivel.percent}%</span>{" "}
                            <span className="text-zinc-400">
                              ({nivel.solved}/{nivel.total})
                            </span>
                          </li>
                        ))}
                      </ul>
                    ) : (
                      "—"
                    )}
                  </td>
                  <td className="px-4 py-2 text-zinc-500">
                    {usuario.lastLoginAt ? formatearFecha(new Date(usuario.lastLoginAt).toISOString()) : "Nunca"}
                  </td>
                  <td className="px-4 py-2 text-right">
                    {usuario.role === "SUPER_ADMIN" ? null : (
                      <div className="flex justify-end gap-3 whitespace-nowrap">
                        {usuario.active ? (
                          <button
                            type="button"
                            disabled={ocupado === usuario.id}
                            onClick={() => void reenviar(usuario)}
                            className="text-xs font-medium text-indigo-600 hover:underline disabled:opacity-50"
                          >
                            Reenviar contraseña
                          </button>
                        ) : null}
                        <button
                          type="button"
                          disabled={ocupado === usuario.id}
                          onClick={() => void cambiarEstado(usuario)}
                          className="text-xs font-medium text-zinc-600 hover:underline disabled:opacity-50"
                        >
                          {usuario.active ? "Desactivar" : "Activar"}
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
