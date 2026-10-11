type Props = {
  valor: number;
  etiqueta?: string;
  className?: string;
};

/** Barra de avance accesible, con el porcentaje ya calculado por el backend. */
export function BarraProgreso({ valor, etiqueta, className = "" }: Props) {
  const porcentaje = Math.min(Math.max(Math.round(valor), 0), 100);

  return (
    <div className={className}>
      {etiqueta ? (
        <div className="mb-1 flex items-center justify-between text-xs text-zinc-500">
          <span>{etiqueta}</span>
          <span className="font-medium text-zinc-700">{porcentaje}%</span>
        </div>
      ) : null}
      <div
        role="progressbar"
        aria-valuenow={porcentaje}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-label={etiqueta ?? "Progreso"}
        className="h-2 w-full overflow-hidden rounded-full bg-zinc-200"
      >
        <div
          className="h-full rounded-full bg-indigo-600 transition-[width] duration-500"
          style={{ width: `${porcentaje}%` }}
        />
      </div>
    </div>
  );
}
