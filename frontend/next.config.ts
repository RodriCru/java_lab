import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  cacheComponents: true,
  // En produccion el navegador habla solo con este sitio y Next reenvia /api/v1/* al backend (BACKEND_URL).
  // Asi la cookie de sesion queda en un unico dominio y SameSite=Lax funciona. En local no hace falta:
  // sin BACKEND_URL no se define ninguna regla.
  async rewrites() {
    const backend = process.env.BACKEND_URL?.replace(/\/+$/, "");
    return backend ? [{ source: "/api/v1/:path*", destination: `${backend}/api/v1/:path*` }] : [];
  },
  partialPrefetching: true,
  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
};

export default nextConfig;
