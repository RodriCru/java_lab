"use client";

import { useCallback, useEffect, useState } from "react";
import { BarraProgreso } from "@/components/BarraProgreso";
import { Estado, EstadoCargando } from "@/components/Estado";
import { api, ApiError } from "@/lib/api";
import { nombreDeDificultad, nombreDeTemaDePreguntas } from "@/lib/format";
import type { QuestionAnswer, QuestionTopic, QuestionView } from "@/lib/types";

type Modo = "" | "PENDING" | "FAILED";

type Sesion = {
  titulo: string;
  preguntas: QuestionView[];
};

const MODOS: { valor: Modo; etiqueta: string }[] = [
  { valor: "", etiqueta: "Todas" },
  { valor: "PENDING", etiqueta: "Sin responder" },
  { valor: "FAILED", etiqueta: "Falladas" },
];

const DIFICULTADES = [
  { valor: "", etiqueta: "Cualquier dificultad" },
  { valor: "easy", etiqueta: "Fácil" },
  { valor: "medium", etiqueta: "Media" },
  { valor: "hard", etiqueta: "Difícil" },
];

const TAMANO_SESION = 100;

export function BancoPreguntas() {
  const [temas, setTemas] = useState<QuestionTopic[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modo, setModo] = useState<Modo>("");
  const [dificultad, setDificultad] = useState("");
  const [sesion, setSesion] = useState<Sesion | null>(null);
  const [iniciando, setIniciando] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  const cargarTemas = useCallback(() => {
    api
      .temasDePreguntas()
      .then((respuesta) => setTemas(respuesta.data))
      .catch((fallo: unknown) =>
        setError(fallo instanceof ApiError ? fallo.message : "No se pudo cargar el banco de preguntas."),
      )
      .finally(() => setCargando(false));
  }, []);

  useEffect(() => {
    cargarTemas();
  }, [cargarTemas]);

  async function empezar(tema: string | undefined, titulo: string) {
    setIniciando(tema ?? "todos");
    setAviso(null);
    try {
      const respuesta = await api.preguntas({
        topic: tema,
        difficulty: dificultad || undefined,
        status: modo || undefined,
        random: true,
        perPage: TAMANO_SESION,
      });
      if (respuesta.data.length === 0) {
        setAviso("No hay preguntas con esos filtros. Prueba con otra combinación.");
      } else {
        setSesion({ titulo, preguntas: respuesta.data });
      }
    } catch (fallo) {
      setAviso(fallo instanceof ApiError ? fallo.message : "No se pudieron cargar las preguntas.");
    } finally {
      setIniciando(null);
    }
  }

  function terminar() {
    setSesion(null);
    cargarTemas();
  }

  if (cargando) {
    return <EstadoCargando mensaje="Cargando el banco de preguntas…" />;
  }

  if (error) {
    return <Estado tono="error" titulo="No se pudo abrir el banco de preguntas" mensaje={error} />;
  }

  if (sesion) {
    return <Cuestionario sesion={sesion} onTerminar={terminar} />;
  }

  const total = temas.reduce((suma, tema) => suma + tema.total, 0);
  const acertadas = temas.reduce((suma, tema) => suma + tema.correct, 0);

  if (total === 0) {
    return (
      <Estado
        titulo="El banco está vacío"
        mensaje="Todavía no hay preguntas cargadas en el servidor."
      />
    );
  }

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-2xl font-bold text-zinc-900">Banco de preguntas</h1>
        <p className="mt-1 text-sm text-zinc-500">
          Preguntas de entrevista sobre Java, Spring y bases de datos. Elige un tema y practica con
          retroalimentación al instante.
        </p>
      </header>

      <div className="rounded-lg border border-zinc-200 bg-white p-4">
        <BarraProgreso
          valor={(acertadas / total) * 100}
          etiqueta={`${acertadas} de ${total} preguntas acertadas`}
        />
        <div className="mt-4 flex flex-wrap gap-3">
          <label className="text-sm text-zinc-600">
            <span className="mr-2">Mostrar</span>
            <select
              value={modo}
              onChange={(evento) => setModo(evento.target.value as Modo)}
              className="rounded-md border border-zinc-300 bg-white px-2 py-1.5 text-sm"
            >
              {MODOS.map((opcion) => (
                <option key={opcion.valor} value={opcion.valor}>
                  {opcion.etiqueta}
                </option>
              ))}
            </select>
          </label>
          <label className="text-sm text-zinc-600">
            <span className="mr-2">Nivel</span>
            <select
              value={dificultad}
              onChange={(evento) => setDificultad(evento.target.value)}
              className="rounded-md border border-zinc-300 bg-white px-2 py-1.5 text-sm"
            >
              {DIFICULTADES.map((opcion) => (
                <option key={opcion.valor} value={opcion.valor}>
                  {opcion.etiqueta}
                </option>
              ))}
            </select>
          </label>
          <button
            type="button"
            disabled={iniciando !== null}
            onClick={() => empezar(undefined, "Mezcla de todos los temas")}
            className="ml-auto rounded-md bg-indigo-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-60"
          >
            {iniciando === "todos" ? "Preparando…" : "Mezclar todos los temas"}
          </button>
        </div>
        {aviso ? (
          <p role="alert" className="mt-3 text-sm text-amber-700">
            {aviso}
          </p>
        ) : null}
      </div>

      <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {temas.map((tema) => {
          const nombre = nombreDeTemaDePreguntas(tema.topic);
          return (
            <li key={tema.topic} className="rounded-lg border border-zinc-200 bg-white p-4">
              <h2 className="font-semibold text-zinc-900">{nombre}</h2>
              <p className="mt-1 text-xs text-zinc-500">
                {tema.total} preguntas · {tema.answered} respondidas
              </p>
              <BarraProgreso
                className="mt-3"
                valor={tema.total === 0 ? 0 : (tema.correct / tema.total) * 100}
                etiqueta="Acertadas"
              />
              <button
                type="button"
                disabled={iniciando !== null}
                onClick={() => empezar(tema.topic, nombre)}
                className="mt-4 w-full rounded-md border border-indigo-200 px-3 py-1.5 text-sm font-medium text-indigo-700 hover:bg-indigo-50 disabled:opacity-60"
              >
                {iniciando === tema.topic ? "Preparando…" : "Practicar"}
              </button>
            </li>
          );
        })}
      </ul>
    </div>
  );
}

function Cuestionario({ sesion, onTerminar }: { sesion: Sesion; onTerminar: () => void }) {
  const [indice, setIndice] = useState(0);
  const [elegida, setElegida] = useState<number | null>(null);
  const [resultado, setResultado] = useState<QuestionAnswer | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [aciertos, setAciertos] = useState(0);
  const [terminado, setTerminado] = useState(false);

  const total = sesion.preguntas.length;
  const pregunta = sesion.preguntas[indice];

  async function comprobar() {
    if (elegida === null || enviando) {
      return;
    }
    setEnviando(true);
    setError(null);
    try {
      const respuesta = await api.responderPregunta(pregunta.id, elegida);
      setResultado(respuesta.data);
      if (respuesta.data.correct) {
        setAciertos((actual) => actual + 1);
      }
    } catch (fallo) {
      setError(fallo instanceof ApiError ? fallo.message : "No se pudo comprobar la respuesta.");
    } finally {
      setEnviando(false);
    }
  }

  function siguiente() {
    if (indice + 1 >= total) {
      setTerminado(true);
      return;
    }
    setIndice(indice + 1);
    setElegida(null);
    setResultado(null);
    setError(null);
  }

  if (terminado) {
    const porcentaje = Math.round((aciertos / total) * 100);
    return (
      <div className="mx-auto max-w-xl space-y-5 text-center">
        <Estado
          titulo={`${aciertos} de ${total} correctas (${porcentaje}%)`}
          mensaje={
            porcentaje >= 80
              ? "Muy bien, dominas este bloque."
              : "Repasa las explicaciones y vuelve a intentarlo con las falladas."
          }
        >
          <button
            type="button"
            onClick={onTerminar}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
          >
            Volver al banco
          </button>
        </Estado>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl space-y-5">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h1 className="text-lg font-bold text-zinc-900">{sesion.titulo}</h1>
          <p className="text-xs text-zinc-500">
            Pregunta {indice + 1} de {total} · {nombreDeTemaDePreguntas(pregunta.topic)} ·{" "}
            {nombreDeDificultad(pregunta.difficulty)}
          </p>
        </div>
        <button type="button" onClick={onTerminar} className="text-sm text-indigo-600 hover:underline">
          Salir
        </button>
      </div>

      <BarraProgreso valor={(indice / total) * 100} etiqueta="Avance" />

      <section className="space-y-4 rounded-lg border border-zinc-200 bg-white p-5">
        <p className="text-base font-medium text-zinc-900">{pregunta.question}</p>

        {pregunta.code ? (
          <pre className="overflow-x-auto rounded-md bg-zinc-900 p-4 text-sm text-zinc-100">
            <code>{pregunta.code}</code>
          </pre>
        ) : null}

        <fieldset className="space-y-2" disabled={resultado !== null}>
          <legend className="sr-only">Opciones</legend>
          {pregunta.options.map((opcion, posicion) => {
            let estilo = "border-zinc-200 hover:bg-zinc-50";
            if (resultado) {
              if (posicion === resultado.correctIndex) {
                estilo = "border-green-500 bg-green-50";
              } else if (posicion === elegida) {
                estilo = "border-red-400 bg-red-50";
              }
            } else if (posicion === elegida) {
              estilo = "border-indigo-500 bg-indigo-50";
            }
            return (
              <label
                key={posicion}
                className={`flex cursor-pointer items-start gap-3 rounded-md border px-4 py-3 text-sm text-zinc-800 ${estilo}`}
              >
                <input
                  type="radio"
                  name={`pregunta-${pregunta.id}`}
                  checked={elegida === posicion}
                  onChange={() => setElegida(posicion)}
                  className="mt-0.5"
                />
                <span>{opcion}</span>
              </label>
            );
          })}
        </fieldset>

        {error ? (
          <p role="alert" className="text-sm text-red-600">
            {error}
          </p>
        ) : null}

        {resultado ? (
          <div
            role="status"
            className={`rounded-md border p-4 text-sm ${
              resultado.correct ? "border-green-200 bg-green-50" : "border-red-200 bg-red-50"
            }`}
          >
            <p className={`font-semibold ${resultado.correct ? "text-green-700" : "text-red-700"}`}>
              {resultado.correct ? "¡Correcto!" : "No es esa."}
            </p>
            <p className="mt-1 text-zinc-700">{resultado.explanation}</p>
          </div>
        ) : null}

        <div className="flex justify-end">
          {resultado ? (
            <button
              type="button"
              onClick={siguiente}
              className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
            >
              {indice + 1 >= total ? "Ver resultado" : "Siguiente →"}
            </button>
          ) : (
            <button
              type="button"
              disabled={elegida === null || enviando}
              onClick={comprobar}
              className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
            >
              {enviando ? "Comprobando…" : "Comprobar"}
            </button>
          )}
        </div>
      </section>
    </div>
  );
}
