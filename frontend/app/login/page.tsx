"use client";

import { useEffect, useState } from "react";
import { FormularioAcceso } from "@/components/FormularioAcceso";

export default function PaginaLogin() {
  const [destino, setDestino] = useState<string | undefined>(undefined);

  // Se lee de la barra de direcciones en el cliente para no forzar render dinamico.
  useEffect(() => {
    const parametros = new URLSearchParams(window.location.search);
    setDestino(parametros.get("destino") ?? undefined);
  }, []);

  return <FormularioAcceso modo="login" destino={destino} />;
}
