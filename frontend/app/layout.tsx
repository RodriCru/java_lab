import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { Suspense } from "react";
import "./globals.css";
import { BarraNavegacion } from "@/components/BarraNavegacion";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Java Practice Lab",
  description:
    "Laboratorio de ejercicios de Java orientado a entrevistas técnicas: lógica, estructuras de datos y resolución de problemas.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html
      lang="es"
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col">
        {/* La barra lee la ruta actual, asi que se renderiza despues del cascaron estatico. */}
        <Suspense fallback={<div className="h-[57px] border-b border-zinc-200 bg-white" />}>
          <BarraNavegacion />
        </Suspense>
        <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-8">{children}</main>
        <footer className="border-t border-zinc-200 py-6 text-center text-sm text-zinc-500">
          Java Practice Lab · practica para tu próxima entrevista técnica
        </footer>
      </body>
    </html>
  );
}
