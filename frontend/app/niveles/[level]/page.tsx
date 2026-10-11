import { Suspense } from "react";
import { EstadoCargando } from "@/components/Estado";
import { VistaNivel } from "@/components/VistaNivel";

export default function PaginaNivel({ params }: PageProps<"/niveles/[level]">) {
  return (
    <Suspense fallback={<EstadoCargando mensaje="Cargando nivel…" />}>
      <Contenido params={params} />
    </Suspense>
  );
}

async function Contenido({ params }: { params: Promise<{ level: string }> }) {
  const { level } = await params;
  const numero = Number.parseInt(level, 10);

  return <VistaNivel level={Number.isNaN(numero) ? 1 : numero} />;
}
