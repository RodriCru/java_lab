"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import { nombreDeDificultad } from "@/lib/format";
import type { ExerciseDetail, SubmissionResult } from "@/lib/types";
import { EditorCodigo } from "./EditorCodigo";
import { Ejemplos } from "./Ejemplos";
import { Estado, EstadoCargando } from "./Estado";
import { ResultadoEnvio } from "./ResultadoEnvio";
import { Teoria } from "./Teoria";

function BotonSiguiente({ slug, titulo }: { slug: string; titulo: string | null }) {
  return (
    <Link
      href={`/ejercicios/${slug}`}
      className="inline-flex items-center gap-2 rounded-md bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700"
    >
      Siguiente ejercicio{titulo ? `: ${titulo}` : ""} →
    </Link>
  );
}

const ALTURA_NORMAL = 420;
const ALTURA_MIN = 240;
const CLAVE_TEORIA = "javalab:teoria-abierta";

type Props = {
  slug: string;
};

/** Espacio de trabajo de un ejercicio: enunciado, editor, pistas y envío. */
export function PanelEjercicio({ slug }: Props) {
  const [ejercicio, setEjercicio] = useState<ExerciseDetail | null>(null);
  const [codigo, setCodigo] = useState("");
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [resultado, setResultado] = useState<SubmissionResult | null>(null);
  const [pistas, setPistas] = useState<string[]>([]);
  const [aviso, setAviso] = useState<string | null>(null);
  const [altura, setAltura] = useState(ALTURA_NORMAL);
  const [teoriaAbierta, setTeoriaAbierta] = useState(true);
  const [respuestaComplejidad, setRespuestaComplejidad] = useState<number | null>(null);

  useEffect(() => {
    try {
      setTeoriaAbierta(localStorage.getItem(CLAVE_TEORIA) !== "0");
    } catch {
      // Sin almacenamiento disponible: la teoría queda abierta.
    }
  }, []);

  function alternarTeoria(abierta: boolean) {
    setTeoriaAbierta(abierta);
    try {
      localStorage.setItem(CLAVE_TEORIA, abierta ? "1" : "0");
    } catch {
      // No es crítico.
    }
  }

  /** Arrastrar el borde inferior del editor para hacerlo más alto o más bajo. */
  function empezarArrastre(evento: React.PointerEvent<HTMLDivElement>) {
    evento.preventDefault();
    const inicioY = evento.clientY;
    const inicioAltura = altura;
    const mover = (mov: PointerEvent) =>
      setAltura(Math.max(ALTURA_MIN, Math.round(inicioAltura + mov.clientY - inicioY)));
    const soltar = () => {
      window.removeEventListener("pointermove", mover);
      window.removeEventListener("pointerup", soltar);
    };
    window.addEventListener("pointermove", mover);
    window.addEventListener("pointerup", soltar);
  }

  useEffect(() => {
    let activo = true;
    setCargando(true);
    setError(null);
    setResultado(null);
    setPistas([]);
    setAviso(null);

    api
      .ejercicio(slug)
      .then((respuesta) => {
        if (!activo) {
          return;
        }
        setEjercicio(respuesta.data);
        setCodigo(respuesta.data.savedCode ?? respuesta.data.starterCode);
      })
      .catch((fallo: unknown) => {
        if (!activo) {
          return;
        }
        setError(
          fallo instanceof ApiError ? fallo.message : "No se pudo cargar el ejercicio.",
        );
      })
      .finally(() => {
        if (activo) {
          setCargando(false);
        }
      });

    return () => {
      activo = false;
    };
  }, [slug]);

  const enviar = useCallback(async () => {
    if (!ejercicio || enviando || codigo.trim().length === 0) {
      return;
    }
    setEnviando(true);
    setAviso(null);
    try {
      const respuesta = await api.enviarSolucion({
        exerciseSlug: ejercicio.slug,
        sourceCode: codigo,
        complexityAnswer: respuestaComplejidad,
      });
      setResultado(respuesta.data);
      setEjercicio((previo) =>
        previo
          ? {
              ...previo,
              solved: respuesta.data.solved,
              attempted: true,
              attempts: previo.attempts + 1,
              savedCode: codigo,
            }
          : previo,
      );
    } catch (fallo: unknown) {
      setAviso(fallo instanceof ApiError ? fallo.message : "No se pudo enviar tu solución.");
    } finally {
      setEnviando(false);
    }
  }, [codigo, ejercicio, enviando, respuestaComplejidad]);

  useEffect(() => {
    function atajo(evento: KeyboardEvent) {
      if ((evento.ctrlKey || evento.metaKey) && evento.key === "Enter") {
        evento.preventDefault();
        void enviar();
      }
    }
    window.addEventListener("keydown", atajo);
    return () => window.removeEventListener("keydown", atajo);
  }, [enviar]);

  async function pedirPista() {
    if (!ejercicio) {
      return;
    }
    const indice = pistas.length;
    setAviso(null);
    try {
      const respuesta = await api.pista(ejercicio.slug, indice);
      setPistas((previas) => [...previas, respuesta.data.hint]);
    } catch (fallo: unknown) {
      setAviso(fallo instanceof ApiError ? fallo.message : "No se pudo obtener la pista.");
    }
  }

  if (cargando) {
    return <EstadoCargando mensaje="Cargando ejercicio…" />;
  }

  if (error || !ejercicio) {
    return (
      <Estado tono="error" titulo="No se pudo abrir el ejercicio" mensaje={error ?? "Ejercicio no encontrado."}>
        <Link
          href="/niveles"
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
        >
          Volver a los niveles
        </Link>
      </Estado>
    );
  }

  const quedanPistas = pistas.length < ejercicio.hintsCount;

  return (
    <div className="space-y-6">
      <div>
        <Link href={`/niveles/${ejercicio.level}`} className="text-sm text-indigo-600 hover:underline">
          ← Nivel {ejercicio.level}
        </Link>
        <div className="mt-2 flex flex-wrap items-center gap-3">
          <h1 className="text-2xl font-bold text-zinc-900">{ejercicio.title}</h1>
          {ejercicio.solved ? (
            <span className="rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-semibold text-emerald-800">
              Resuelto
            </span>
          ) : null}
          {ejercicio.solved && ejercicio.nextSlug ? (
            <Link
              href={`/ejercicios/${ejercicio.nextSlug}`}
              className="rounded-md border border-indigo-300 px-3 py-1 text-xs font-medium text-indigo-700 hover:bg-indigo-50"
            >
              Siguiente →
            </Link>
          ) : null}
          {!ejercicio.solved && ejercicio.attempted ? (
            <span className="rounded-full bg-amber-100 px-2.5 py-0.5 text-xs font-semibold text-amber-800">
              En progreso
            </span>
          ) : null}
        </div>
        <p className="mt-1 text-sm text-zinc-500">
          {ejercicio.topicName} · {nombreDeDificultad(ejercicio.difficulty)} ·{" "}
          {ejercicio.testsCount} tests · {ejercicio.estimatedMinutes ?? "?"} min estimados
        </p>
      </div>

      <div className="space-y-6">
        <div className="space-y-4">
          <section className="rounded-lg border border-zinc-200 bg-white p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-zinc-500">Enunciado</h2>
            <div className="mt-3">
              <Ejemplos statement={ejercicio.statement} />
            </div>
            <div className="mt-4 rounded-md bg-zinc-50 p-3">
              <p className="text-xs font-medium text-zinc-500">Firma esperada</p>
              <code className="mt-1 block font-mono text-xs break-words text-indigo-700">
                {ejercicio.methodSignature}
              </code>
            </div>
          </section>

          {ejercicio.topicTheory ? (
            <details
              open={teoriaAbierta}
              onToggle={(evento) => alternarTeoria(evento.currentTarget.open)}
              className="rounded-lg border border-indigo-200 bg-indigo-50/30 p-5"
            >
              <summary className="cursor-pointer text-sm font-semibold uppercase tracking-wide text-indigo-700">
                Teoría: {ejercicio.topicName}
              </summary>
              <div className="mt-4">
                <Teoria markdown={ejercicio.topicTheory} />
              </div>
            </details>
          ) : null}

          <section className="rounded-lg border border-zinc-200 bg-white p-5">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold uppercase tracking-wide text-zinc-500">Pistas</h2>
              <span className="text-xs text-zinc-400">
                {pistas.length} de {ejercicio.hintsCount}
              </span>
            </div>
            {pistas.length === 0 ? (
              <p className="mt-3 text-sm text-zinc-500">
                Inténtalo primero sin ayuda. Si te atascas, pide una pista.
              </p>
            ) : (
              <ol className="mt-3 space-y-3">
                {pistas.map((pista, indice) => (
                  <li key={indice} className="rounded-md border-l-4 border-indigo-300 bg-indigo-50/60 p-3">
                    <p className="text-xs font-semibold text-indigo-700">Pista {indice + 1}</p>
                    <p className="mt-1 text-sm text-zinc-800">{pista}</p>
                  </li>
                ))}
              </ol>
            )}
            <button
              type="button"
              onClick={pedirPista}
              disabled={!quedanPistas}
              className="mt-4 rounded-md border border-indigo-300 px-3 py-1.5 text-sm font-medium text-indigo-700 hover:bg-indigo-50 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {quedanPistas ? "Ver siguiente pista" : "No hay más pistas"}
            </button>
          </section>
        </div>

        <div className="space-y-4">
          <section className="rounded-lg border border-zinc-200 bg-white p-5">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <h2 className="text-sm font-semibold uppercase tracking-wide text-zinc-500">
                Tu solución
              </h2>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => setAltura(altura > ALTURA_NORMAL ? ALTURA_NORMAL : 800)}
                  className="rounded-md border border-zinc-300 px-3 py-1.5 text-sm text-zinc-700 hover:bg-zinc-100"
                >
                  {altura > ALTURA_NORMAL ? "Reducir editor" : "Ampliar editor"}
                </button>
                <button
                  type="button"
                  onClick={() => setCodigo(ejercicio.starterCode)}
                  className="rounded-md border border-zinc-300 px-3 py-1.5 text-sm text-zinc-700 hover:bg-zinc-100"
                >
                  Reiniciar código
                </button>
                <button
                  type="button"
                  onClick={enviar}
                  disabled={enviando || codigo.trim().length === 0}
                  className="rounded-md bg-indigo-600 px-4 py-1.5 text-sm font-semibold text-white hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {enviando ? "Ejecutando tests…" : "Enviar solución"}
                </button>
              </div>
            </div>
            <p className="mt-2 text-xs text-zinc-500">
              Atajo: {typeof navigator !== "undefined" && navigator.platform.includes("Mac") ? "⌘" : "Ctrl"}
              +Enter para enviar.
            </p>
            <div className="mt-3">
              <EditorCodigo valor={codigo} onChange={setCodigo} altura={altura} lenguaje={ejercicio.language} />
              <div
                role="separator"
                aria-orientation="horizontal"
                aria-label="Arrastra para cambiar la altura del editor"
                onPointerDown={empezarArrastre}
                className="mt-1 flex h-3 cursor-row-resize touch-none items-center justify-center"
              >
                <span className="h-1 w-16 rounded-full bg-zinc-300" />
              </div>
            </div>
          </section>

          {ejercicio.complexityQuestion ? (
            <section className="rounded-lg border border-zinc-200 bg-white p-5">
              <h2 className="text-sm font-semibold uppercase tracking-wide text-zinc-500">
                Análisis de complejidad
              </h2>
              <fieldset className="mt-3">
                <legend className="text-sm text-zinc-800">{ejercicio.complexityQuestion.prompt}</legend>
                <div className="mt-3 space-y-2">
                  {ejercicio.complexityQuestion.options.map((opcion, indice) => (
                    <label key={indice} className="flex cursor-pointer items-center gap-2 text-sm text-zinc-700">
                      <input
                        type="radio"
                        name="complejidad"
                        checked={respuestaComplejidad === indice}
                        onChange={() => setRespuestaComplejidad(indice)}
                      />
                      {opcion}
                    </label>
                  ))}
                </div>
              </fieldset>
              <p className="mt-3 text-xs text-zinc-500">
                Se evalúa junto con tu código cuando los tests pasan.
              </p>
              {resultado?.complexity ? (
                <div
                  role="status"
                  className={`mt-3 rounded-md border px-3 py-2 text-sm ${
                    resultado.complexity.correct
                      ? "border-emerald-200 bg-emerald-50 text-emerald-800"
                      : "border-amber-200 bg-amber-50 text-amber-800"
                  }`}
                >
                  {!resultado.complexity.answered
                    ? "Aún no respondiste la pregunta de complejidad."
                    : resultado.complexity.correct
                      ? "¡Correcto!"
                      : "No es esa."}
                  {resultado.complexity.explanation ? <p className="mt-1">{resultado.complexity.explanation}</p> : null}
                </div>
              ) : null}
            </section>
          ) : null}

          {aviso ? (
            <div role="alert" className="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
              {aviso}
            </div>
          ) : null}

          {enviando ? (
            <div className="rounded-lg border border-zinc-200 bg-white px-4 py-6 text-center">
              <div className="mx-auto h-6 w-6 animate-spin rounded-full border-2 border-zinc-300 border-t-indigo-600" />
              <p className="mt-3 text-sm text-zinc-600">
                Compilando y ejecutando los tests en un contenedor aislado…
              </p>
            </div>
          ) : null}

          {resultado ? <ResultadoEnvio resultado={resultado} /> : null}
          {resultado?.status === "PASSED" ? (
            ejercicio.nextSlug ? (
              <BotonSiguiente slug={ejercicio.nextSlug} titulo={ejercicio.nextTitle} />
            ) : (
              <p className="text-sm font-medium text-emerald-700">¡Terminaste el último ejercicio del catálogo!</p>
            )
          ) : null}
        </div>
      </div>
    </div>
  );
}
