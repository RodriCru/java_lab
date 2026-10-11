import type { ReactNode } from "react";

/** Convierte `código` y **negritas** de una línea en elementos React (sin HTML crudo). */
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

type Bloque =
  | { tipo: "titulo"; texto: string }
  | { tipo: "parrafo"; texto: string }
  | { tipo: "lista"; items: string[] }
  | { tipo: "codigo"; texto: string };

function leerBloques(markdown: string): Bloque[] {
  const bloques: Bloque[] = [];
  const lineas = markdown.replace(/\r\n/g, "\n").split("\n");
  let i = 0;

  while (i < lineas.length) {
    const linea = lineas[i];

    if (linea.trim() === "") {
      i++;
    } else if (linea.startsWith("```")) {
      const codigo: string[] = [];
      i++;
      while (i < lineas.length && !lineas[i].startsWith("```")) {
        codigo.push(lineas[i++]);
      }
      i++;
      bloques.push({ tipo: "codigo", texto: codigo.join("\n") });
    } else if (/^#{1,4}\s/.test(linea)) {
      bloques.push({ tipo: "titulo", texto: linea.replace(/^#{1,4}\s+/, "") });
      i++;
    } else if (/^\s*[-*]\s/.test(linea)) {
      const items: string[] = [];
      while (i < lineas.length && /^\s*[-*]\s/.test(lineas[i])) {
        items.push(lineas[i++].replace(/^\s*[-*]\s+/, ""));
      }
      bloques.push({ tipo: "lista", items });
    } else {
      const partes: string[] = [];
      while (
        i < lineas.length &&
        lineas[i].trim() !== "" &&
        !lineas[i].startsWith("```") &&
        !/^#{1,4}\s/.test(lineas[i]) &&
        !/^\s*[-*]\s/.test(lineas[i])
      ) {
        partes.push(lineas[i++]);
      }
      bloques.push({ tipo: "parrafo", texto: partes.join(" ") });
    }
  }
  return bloques;
}

/** Muestra la teoría de un tema (Markdown sencillo: títulos, párrafos, listas y código). */
export function Teoria({ markdown }: { markdown: string }) {
  return (
    <div className="space-y-3 text-sm leading-relaxed text-zinc-800">
      {leerBloques(markdown).map((bloque, i) => {
        switch (bloque.tipo) {
          case "titulo":
            return (
              <h3 key={i} className="pt-1 font-semibold text-zinc-900">
                {enLinea(bloque.texto)}
              </h3>
            );
          case "lista":
            return (
              <ul key={i} className="list-disc space-y-1 pl-5">
                {bloque.items.map((item, j) => (
                  <li key={j}>{enLinea(item)}</li>
                ))}
              </ul>
            );
          case "codigo":
            return (
              <pre key={i} className="overflow-x-auto rounded-md bg-zinc-900 p-3 font-mono text-xs leading-relaxed text-zinc-100">
                {bloque.texto}
              </pre>
            );
          default:
            return <p key={i}>{enLinea(bloque.texto)}</p>;
        }
      })}
    </div>
  );
}
