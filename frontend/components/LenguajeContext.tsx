"use client";

import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";

/** Lenguajes que el laboratorio puede mostrar. El orden es el de las pestañas/selector. */
export const LENGUAJES = [
  { id: "java", nombre: "Java" },
  { id: "python", nombre: "Python" },
] as const;

export type LenguajeId = (typeof LENGUAJES)[number]["id"];

const CLAVE = "javalab.lenguaje";
const POR_DEFECTO: LenguajeId = "java";

function esLenguajeValido(valor: string | null): valor is LenguajeId {
  return valor !== null && LENGUAJES.some((l) => l.id === valor);
}

type Contexto = {
  lenguaje: LenguajeId;
  cambiarLenguaje: (lenguaje: LenguajeId) => void;
  /** Falso hasta que se lee localStorage; evita pedir datos con el lenguaje equivocado. */
  listo: boolean;
};

const LenguajeContext = createContext<Contexto | null>(null);

export function ProveedorLenguaje({ children }: { children: ReactNode }) {
  const [lenguaje, setLenguaje] = useState<LenguajeId>(POR_DEFECTO);
  const [listo, setListo] = useState(false);

  // Se lee en un efecto (no en el render) porque localStorage no existe en el servidor.
  useEffect(() => {
    const guardado = window.localStorage.getItem(CLAVE);
    if (esLenguajeValido(guardado)) {
      setLenguaje(guardado);
    }
    setListo(true);
  }, []);

  const cambiarLenguaje = useCallback((nuevo: LenguajeId) => {
    setLenguaje(nuevo);
    window.localStorage.setItem(CLAVE, nuevo);
  }, []);

  return (
    <LenguajeContext.Provider value={{ lenguaje, cambiarLenguaje, listo }}>
      {children}
    </LenguajeContext.Provider>
  );
}

export function useLenguaje(): Contexto {
  const contexto = useContext(LenguajeContext);
  if (!contexto) {
    throw new Error("useLenguaje debe usarse dentro de <ProveedorLenguaje>");
  }
  return contexto;
}

/** Nombre visible de un lenguaje (para títulos); si no se reconoce, devuelve el id tal cual. */
export function nombreDeLenguaje(id: string): string {
  return LENGUAJES.find((l) => l.id === id)?.nombre ?? id;
}
