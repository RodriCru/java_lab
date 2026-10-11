"use client";

import type { SubmissionResult } from "@/lib/types";
import { formatearDuracion, nombreDeTest } from "@/lib/format";

const ESTILO_ESTADO: Record<SubmissionResult["status"], string> = {
  PASSED: "border-emerald-200 bg-emerald-50 text-emerald-800",
  FAILED: "border-amber-200 bg-amber-50 text-amber-800",
  COMPILE_ERROR: "border-red-200 bg-red-50 text-red-800",
  TIMEOUT: "border-orange-200 bg-orange-50 text-orange-800",
  INTERNAL_ERROR: "border-zinc-200 bg-zinc-50 text-zinc-700",
};

const TITULO_ESTADO: Record<SubmissionResult["status"], string> = {
  PASSED: "¡Todos los tests pasaron!",
  FAILED: "Algunos tests fallaron",
  COMPILE_ERROR: "Tu código no compiló",
  TIMEOUT: "Tu solución tardó demasiado",
  INTERNAL_ERROR: "No se pudieron ejecutar los tests",
};

export function ResultadoEnvio({ resultado }: { resultado: SubmissionResult }) {
  return (
    <section className="space-y-4" aria-live="polite">
      <div className={`rounded-lg border px-4 py-3 ${ESTILO_ESTADO[resultado.status]}`}>
        <div className="flex flex-wrap items-center justify-between gap-2">
          <p className="font-semibold">{TITULO_ESTADO[resultado.status]}</p>
          <p className="text-xs">
            {resultado.passed} de {resultado.total} tests · {formatearDuracion(resultado.durationMillis)} ·
            motor {resultado.backend}
          </p>
        </div>
      </div>

      {resultado.compileOutput ? (
        <div>
          <h3 className="mb-1 text-sm font-semibold text-zinc-800">Errores de compilación</h3>
          <pre className="max-h-72 overflow-auto rounded-lg border border-red-200 bg-red-50 p-3 font-mono text-xs leading-relaxed text-red-800">
            {resultado.compileOutput}
          </pre>
        </div>
      ) : null}

      {resultado.tests.length > 0 ? (
        <div>
          <h3 className="mb-2 text-sm font-semibold text-zinc-800">Detalle de los tests</h3>
          <ul className="divide-y divide-zinc-100 overflow-hidden rounded-lg border border-zinc-200 bg-white">
            {resultado.tests.map((test) => (
              <li key={test.name} className="px-4 py-3">
                <div className="flex items-start gap-3">
                  <span
                    aria-hidden
                    className={`mt-0.5 grid h-5 w-5 shrink-0 place-items-center rounded-full text-xs font-bold text-white ${
                      test.skipped ? "bg-zinc-400" : test.passed ? "bg-emerald-600" : "bg-red-600"
                    }`}
                  >
                    {test.skipped ? "–" : test.passed ? "✓" : "✕"}
                  </span>
                  <div className="min-w-0 flex-1">
                    <p className="text-sm font-medium text-zinc-800">
                      {nombreDeTest(test.name)}
                      <span className="sr-only">
                        {test.passed ? ": pasó" : ": falló"}
                      </span>
                    </p>
                    {test.message ? (
                      <p className="mt-1 font-mono text-xs break-words text-red-700">{test.message}</p>
                    ) : null}
                    {test.input || test.output ? (
                      <details className="mt-2 text-xs" open={!test.passed}>
                        <summary className="cursor-pointer text-zinc-500">Ver entrada y salida</summary>
                        {test.input ? (
                          <>
                            <p className="mt-2 font-semibold text-zinc-600">Entrada (el test)</p>
                            <pre className="mt-1 overflow-auto rounded bg-zinc-50 p-2 font-mono text-zinc-800">
                              {test.input}
                            </pre>
                          </>
                        ) : null}
                        {test.output ? (
                          <>
                            <p className="mt-2 font-semibold text-zinc-600">Tu código imprimió</p>
                            <pre className="mt-1 overflow-auto rounded bg-zinc-50 p-2 font-mono whitespace-pre-wrap text-zinc-800">
                              {test.output}
                            </pre>
                          </>
                        ) : null}
                      </details>
                    ) : null}
                  </div>
                  <span className="shrink-0 text-xs text-zinc-400">
                    {formatearDuracion(test.durationMillis)}
                  </span>
                </div>
              </li>
            ))}
          </ul>
        </div>
      ) : null}

      {resultado.consoleOutput ? (
        <details className="rounded-lg border border-zinc-200 bg-white">
          <summary className="cursor-pointer px-4 py-2 text-sm font-medium text-zinc-700">
            Ver salida de consola
          </summary>
          <pre className="max-h-64 overflow-auto border-t border-zinc-100 p-3 font-mono text-xs leading-relaxed text-zinc-700">
            {resultado.consoleOutput}
          </pre>
        </details>
      ) : null}
    </section>
  );
}
