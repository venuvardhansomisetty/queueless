import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// The React app runs on port 3000 (the backend allows this port in its CORS settings).
export default defineConfig({
  plugins: [react()],
  server: { port: 3000 },
});
