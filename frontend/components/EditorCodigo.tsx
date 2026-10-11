"use client";

import dynamic from "next/dynamic";
import { Component, useEffect, useState, type ReactNode } from "react";

const MonacoEditor = dynamic(() => import("@monaco-editor/react"), {
  ssr: false,
  loading: () => null,
});

/** Si Monaco no carga en este tiempo (por ejemplo, sin internet), se usa el editor simple. */
const ESPERA_MAXIMA_MS = 8_000;

/**
 * Atrapa los fallos de Monaco (por ejemplo «InstantiationService has been disposed», que aparece
 * si React desmonta y reutiliza la pantalla mientras el editor se esta destruyendo) para que el
 * resto de la pagina siga funcionando con el editor simple.
 */
class LimiteDeErrorMonaco extends Component<
  { alFallar: () => ReactNode; children: ReactNode },
  { fallo: boolean }
> {
  state = { fallo: false };

  static getDerivedStateFromError() {
    return { fallo: true };
  }

  render() {
    return this.state.fallo ? this.props.alFallar() : this.props.children;
  }
}

type Props = {
  valor: string;
  onChange: (valor: string) => void;
  altura?: number;
};

/**
 * Editor de Java.
 *
 * <p>Monaco se descarga en el navegador. Para que el laboratorio nunca se quede sin editor,
 * mientras carga (y si falla) se muestra un area de texto equivalente.
 */
export function EditorCodigo({ valor, onChange, altura = 420 }: Props) {
  const [monacoListo, setMonacoListo] = useState(false);
  const [monacoDescartado, setMonacoDescartado] = useState(false);
  const [intento, setIntento] = useState(0);

  useEffect(() => {
    let activo = true;

    import("@monaco-editor/react")
      .then((modulo) => modulo.loader.init())
      .then(() => {
        if (activo) {
          setMonacoListo(true);
        }
      })
      .catch(() => {
        if (activo) {
          setMonacoDescartado(true);
        }
      });

    return () => {
      activo = false;
    };
  }, []);

  useEffect(() => {
    if (monacoListo || monacoDescartado) {
      return;
    }
    const temporizador = setTimeout(
      () => setMonacoDescartado(true),
      ESPERA_MAXIMA_MS,
    );
    return () => clearTimeout(temporizador);
  }, [monacoListo, monacoDescartado]);

  const editorSimple = (aviso: ReactNode) => (
    <div>
      <textarea
        value={valor}
        onChange={(evento) => onChange(evento.target.value)}
        spellCheck={false}
        aria-label="Código de tu solución"
        style={{ height: `${altura}px` }}
        className="w-full resize-y rounded-lg border border-zinc-300 bg-white p-4 font-mono text-sm leading-relaxed text-zinc-800 focus:border-indigo-500 focus:outline-none"
      />
      <p className="mt-1 text-xs text-zinc-500">{aviso}</p>
    </div>
  );

  if (monacoListo && !monacoDescartado) {
    return (
      <LimiteDeErrorMonaco
        key={intento}
        alFallar={() =>
          editorSimple(
            <>
              El editor con resaltado falló, así que estás usando el editor
              simple. Funciona igual.{" "}
              <button
                type="button"
                onClick={() => setIntento((n) => n + 1)}
                className="font-medium text-indigo-600 underline"
              >
                Reintentar editor
              </button>
            </>,
          )
        }
      >
        <div className="overflow-hidden rounded-lg border border-zinc-300">
          <MonacoEditor
            height={`${altura}px`}
            language="java"
            theme="vs"
            value={valor}
            onChange={(nuevo) => onChange(nuevo ?? "")}
            options={{
              fontSize: 14,
              minimap: { enabled: false },
              scrollBeyondLastLine: false,
              tabSize: 4,
              insertSpaces: true,
              renderLineHighlight: "line",
              automaticLayout: true,
              wordWrap: "on",
            }}
          />
        </div>
      </LimiteDeErrorMonaco>
    );
  }

  return editorSimple(
    monacoDescartado
      ? "No se pudo cargar Monaco, así que estás usando el editor simple. Funciona igual."
      : "Cargando el editor con resaltado de sintaxis…",
  );
}
