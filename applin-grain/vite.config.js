import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// El proxy evita problemas de CORS en desarrollo.
// /api/products  ->  http://localhost:8080/api/products (ms-pedidos360-catalog)
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
});
