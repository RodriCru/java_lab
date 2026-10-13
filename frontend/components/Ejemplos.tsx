import type { ReactNode } from "react";

/**
 * Añade un carácter "ancla" al inicio de cada línea para que los espacios en blanco
 * de la izquierda no los colapse el navegador (en HTML los espacios iniciales se ignoran).
 * El ancla se pinta con `visibility: hidden`, así que ocupa su espacio real.
 */
function conAncla(texto: string): ReactNode[] {
  return texto.split("\n").map((linea, i) => (
    <span key={i}>
      {i > 0 ? "\n" : null}
      <span aria-hidden="true" className="invisible">
        ·
      </span>
      {linea}
    </span>
  ));
}

/** Bloque de ejemplo dentro del enunciado. */
type Bloque =
  | { tipo: "texto"; texto: string }
  | { tipo: "ejemplos"; ejemplos: Ejemplo[] }
  | { tipo: "consola"; llamadas: string[]; salida: string };

type Ejemplo =
  | { tipo: "consola"; llamada: string; resultado: string }
  | { tipo: "impresion"; llamada: string; salida: string };

/**
 * Convierte `código`, **negritas** y **un salto de línea** en elementos React (sin HTML crudo).
 * Mantiene el tratamiento que ya usa `Teoria` para las líneas no-ejemplo.
 */
function enLinea(texto: string): ReactNode[] {
  return texto.split(/(`[^`]+`|\*\*[^*]+\*\*)/g).map((trozo, i) => {
    if (trozo.startsWith("`") && trozo.endsWith("`") && trozo.length > 2) {
      return (
        <code key={i} className="rounded bg-zinc-100 px-1 py-0.5 font-mono text-[0.85em] text-indigo-700">
          {trozo.slice(1, -1)}
        </code>
      );
    }
    if (trozo.startsWith("**") && trozo.endsWith("**") && trozo.length > 4) {
      return <strong key={i}>{trozo.slice(2, -2)}</strong>;
    }
    return trozo;
  });
}

/** Reemplaza los escapes `\n` de un YAML por saltos reales para mostrarlos en la consola. */
function saltosReales(texto: string): string {
  return texto.replace(/\\n/g, "\n").replace(/\\t/g, "\t");
}

/** Limpia un valor de resultado: sin corchetes de cita de YAML ni comillas envolventes. */
function limpiarResultado(valor: string): string {
  return valor.trim().replace(/^\[\d+\]\s*/, "").replace(/^"(.*)"$/, "$1");
}

/** Trocea un enunciado en bloques: texto normal, bloques de consola y listas de ejemplos. */
function leerBloques(statement: string): Bloque[] {
  const bloques: Bloque[] = [];
  const lineas = statement.replace(/\r\n/g, "\n").split("\n");
  let i = 0;

  while (i < lineas.length) {
    const linea = lineas[i];

    if (linea.trim() === "") {
      i++;
    } else if (linea.trim().startsWith("```")) {
      const cerca = linea.trim().slice(3).trim();
      const contenido: string[] = [];
      i++;
      while (i < lineas.length && !lineas[i].trim().startsWith("```")) {
        contenido.push(lineas[i++]);
      }
      i++;
      if (cerca === "java") {
        // Bloque de llamada: espera una salida en el bloque siguiente.
        const salida: string[] = [];
        let j = i;
        while (j < lineas.length && lineas[j].trim() === "") j++;
        if (j < lineas.length && lineas[j].trim().startsWith("```java-lab")) {
          j++;
          while (j < lineas.length && !lineas[j].trim().startsWith("```")) {
            salida.push(lineas[j++]);
          }
          i = j + 1;
          bloques.push({ tipo: "consola", llamadas: contenido, salida: salida.join("\n") });
        } else {
          // Bloque suelto: se trata como valor de retorno.
          bloques.push({ tipo: "ejemplos", ejemplos: contenido.map((c) => ({
            tipo: "consola", llamada: c, resultado: "", // el texto del bloque ya es el valor
          })) });
        }
      } else {
        // Bloque de salida suelto: consola directa.
        bloques.push({ tipo: "consola", llamadas: [], salida: contenido.join("\n") });
      }
    } else if (/^\s*[-*]\s/.test(linea)) {
      const items: Ejemplo[] = [];
      while (i < lineas.length && /^\s*[-*]\s/.test(lineas[i])) {
        items.push(itemDeEjemplo(lineas[i++].replace(/^\s*[-*]\s+/, "")));
      }
      bloques.push({ tipo: "ejemplos", ejemplos: items });
    } else {
      const partes: string[] = [];
      while (
        i < lineas.length &&
        lineas[i].trim() !== "" &&
        !lineas[i].trim().startsWith("```") &&
        !/^\s*[-*]\s/.test(lineas[i])
      ) {
        partes.push(lineas[i++]);
      }
      bloques.push({ tipo: "texto", texto: partes.join(" ") });
    }
  }
  return bloques;
}

/**
 * Interpreta una línea de ejemplo. Formatos admitidos:
 *   - `nombre(args) imprime: <salida>`        -> ejemplo de consola (impresión)
 *   - `nombre(args) -> <resultado>`           -> ejemplo de retorno
 *   - `Entrada: ... -> Salida: ...`           -> ejemplo de retorno con etiquetas
 *   - texto suelto                           -> ejemplo de retorno (una sola llamada)
 */
function itemDeEjemplo(item: string): Ejemplo {
  const impresion = item.match(/^(.*?)\s+imprime:\s*(.*)$/i);
  if (impresion) {
    return { tipo: "impresion", llamada: impresion[1].trim(), salida: impresion[2].trim() };
  }

  const flecha = item.match(/^(.*?)\s*(?:->|→)\s*(.*)$/);
  if (flecha) {
    const izquierda = flecha[1].trim().replace(/^Entrada:\s*/i, "").trim();
    const derecha = flecha[2].trim().replace(/^Salida:\s*/i, "").trim();
    return { tipo: "consola", llamada: limpiarResultado(izquierda), resultado: limpiarResultado(derecha) };
  }

  return { tipo: "consola", llamada: item.trim(), resultado: "" };
}

/** Consola de Java: command prompt verde suave sobre fondo oscuro y la salida monoespaciada. */
function Consola({ llamadas, salida }: { llamadas: string[]; salida: string }) {
  return (
    <div className="overflow-x-auto rounded-md bg-zinc-900 p-3 font-mono text-xs leading-relaxed text-zinc-100">
      {llamadas.map((llamada, i) => (
        <div key={i} className="text-zinc-400">
          <span className="text-indigo-400">&gt; </span>
          {llamada}
        </div>
      ))}
      {salida ? (
        <pre className="whitespace-pre text-zinc-100">{conAncla(saltosReales(salida))}</pre>
      ) : null}
    </div>
  );
}

/** Renderiza el enunciado mostrando los ejemplos como una consola de Java. */
export function Ejemplos({ statement }: { statement: string }) {
  return (
    <div className="space-y-3 text-sm leading-relaxed text-zinc-800">
      {leerBloques(statement).map((bloque, i) => {
        switch (bloque.tipo) {
          case "consola":
            return <Consola key={i} llamadas={bloque.llamadas} salida={bloque.salida} />;
          case "ejemplos":
            return (
              <div key={i} className="space-y-1">
                {bloque.ejemplos.map((ejemplo, j) =>
                  ejemplo.tipo === "impresion" ? (
                    <div key={j} className="space-y-0.5">
                      <p className="text-zinc-700">
                        <code className="rounded bg-zinc-100 px-1 py-0.5 font-mono text-[0.85em] text-indigo-700">
                          {ejemplo.llamada}
                        </code>{" "}
                        imprime:
                      </p>
                      <Consola llamadas={[]} salida={saltoOJuego(ejemplo.salida)} />
                    </div>
                  ) : (
                    <div key={j} className="flex flex-wrap items-baseline gap-x-2 gap-y-0.5">
                      <code className="font-mono text-xs text-zinc-900">{ejemplo.llamada}</code>
                      {ejemplo.resultado ? (
                        <>
                          <span className="font-mono text-xs text-indigo-500">→</span>
                          <code className="font-mono text-xs text-indigo-700">{ejemplo.resultado}</code>
                        </>
                      ) : null}
                    </div>
                  ),
                )}
              </div>
            );
          default:
            return (
              <p key={i} className="whitespace-pre-wrap">
                {enLinea(bloque.texto)}
              </p>
            );
        }
      })}
    </div>
  );
}

/** Convierte una descripción de salida (p. ej. `1, 2, 3, uno por línea`) en texto de consola. */
function saltoOJuego(salida: string): string {
  const conComas = salida.match(/^(.*?),\s*(?:uno|una)\s+por\s+l[íi]nea$/i);
  if (conComas) {
    return conComas[1].split(",").map((v) => v.trim()).filter(Boolean).join("\n");
  }
  return salida;
}
