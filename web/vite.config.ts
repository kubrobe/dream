import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// In development the API (server/index.mjs) runs on :3000; Vite forwards /api to it.
export default defineConfig({
  plugins: [react()],
  build: { chunkSizeWarningLimit: 900 },
  server: { proxy: { "/api": "http://localhost:3000" } },
});
