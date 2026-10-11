type Props = {
  titulo: string;
  mensaje: string;
  /** Accion opcional, por ejemplo un boton de reintentar. */
  children?: React.ReactNode;
  tono?: "neutro" | "error";
};

/** Estado de carga, vacio o error. Se usa en todas las vistas para no dejar pantallas en blanco. */
export function Estado({ titulo, mensaje, children, tono = "neutro" }: Props) {
  const esError = tono === "error";

  return (
    <div
      role={esError ? "alert" : "status"}
      className={`rounded-lg border px-6 py-10 text-center ${
        esError ? "border-red-200 bg-red-50" : "border-zinc-200 bg-white"
      }`}
    >
      <p className={`text-base font-semibold ${esError ? "text-red-700" : "text-zinc-800"}`}>
        {titulo}
      </p>
      <p className={`mt-2 text-sm ${esError ? "text-red-600" : "text-zinc-500"}`}>{mensaje}</p>
      {children ? <div className="mt-5 flex justify-center gap-3">{children}</div> : null}
    </div>
  );
}

export function EstadoCargando({ mensaje = "Cargando…" }: { mensaje?: string }) {
  return (
    <div role="status" className="rounded-lg border border-zinc-200 bg-white px-6 py-10 text-center">
      <div className="mx-auto h-6 w-6 animate-spin rounded-full border-2 border-zinc-300 border-t-indigo-600" />
      <p className="mt-3 text-sm text-zinc-500">{mensaje}</p>
    </div>
  );
}
