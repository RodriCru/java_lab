import { Suspense } from "react";
import { EstadoCargando } from "@/components/Estado";
import { PanelEjercicio } from "@/components/PanelEjercicio";

export default function PaginaEjercicio({ params }: PageProps<"/ejercicios/[slug]">) {
  return (
    <Suspense fallback={<EstadoCargando mensaje="Cargando ejercicio…" />}>
      <Contenido params={params} />
    </Suspense>
  );
}

async function Contenido({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;

  return <PanelEjercicio slug={slug} />;
}
