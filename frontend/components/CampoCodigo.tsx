"use client";

import { useRef } from "react";

type Props = {
  valor: string;
  onCambio: (valor: string) => void;
  disabled?: boolean;
  longitud?: number;
};

/** Casillas para un codigo numerico (OTP): avanza solo, retrocede con borrar y acepta pegar el codigo completo. */
export function CampoCodigo({ valor, onCambio, disabled, longitud = 6 }: Props) {
  const casillas = useRef<Array<HTMLInputElement | null>>([]);

  function enfocar(indice: number) {
    casillas.current[Math.min(Math.max(indice, 0), longitud - 1)]?.focus();
  }

  function escribir(indice: number, texto: string) {
    const digitos = texto.replace(/\D/g, "");
    if (!digitos) {
      return;
    }
    // Pegar varios digitos (o autocompletar) rellena desde la casilla actual.
    const nuevo = (valor.slice(0, indice) + digitos).slice(0, longitud);
    onCambio(nuevo);
    enfocar(nuevo.length < longitud ? nuevo.length : longitud - 1);
  }

  function teclear(indice: number, evento: React.KeyboardEvent<HTMLInputElement>) {
    if (evento.key === "Backspace") {
      evento.preventDefault();
      if (valor[indice]) {
        onCambio(valor.slice(0, indice) + valor.slice(indice + 1));
      } else {
        onCambio(valor.slice(0, Math.max(indice - 1, 0)) + valor.slice(indice));
        enfocar(indice - 1);
      }
    } else if (evento.key === "ArrowLeft") {
      enfocar(indice - 1);
    } else if (evento.key === "ArrowRight") {
      enfocar(indice + 1);
    }
  }

  return (
    <div className="flex justify-center gap-2" role="group" aria-label="Código de verificación">
      {Array.from({ length: longitud }, (_, indice) => (
        <input
          key={indice}
          ref={(elemento) => {
            casillas.current[indice] = elemento;
          }}
          type="text"
          inputMode="numeric"
          autoComplete={indice === 0 ? "one-time-code" : "off"}
          maxLength={longitud}
          disabled={disabled}
          aria-label={`Dígito ${indice + 1}`}
          value={valor[indice] ?? ""}
          onChange={(evento) => escribir(indice, evento.target.value)}
          onKeyDown={(evento) => teclear(indice, evento)}
          onFocus={(evento) => evento.target.select()}
          className="h-12 w-11 rounded-lg border border-zinc-300 bg-white text-center font-mono text-xl font-semibold text-zinc-900 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200 disabled:opacity-60"
        />
      ))}
    </div>
  );
}
