import react from '@vitejs/plugin-react';
import { defineConfig, loadEnv } from 'vite';
import fs from 'node:fs';
import path from 'node:path';

const readHttpsConfig = () => {
  const certPath = path.resolve(
    '../ovara-backend/src/main/resources/localhost-cert.pem',
  );
  const keyPath = path.resolve(
    '../ovara-backend/src/main/resources/localhost-key.pem',
  );

  if (!fs.existsSync(certPath) || !fs.existsSync(keyPath)) {
    return undefined;
  }

  return {
    cert: fs.readFileSync(certPath),
    key: fs.readFileSync(keyPath),
  };
};

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const ovaraBackend = env.VITE_OVARA_BACKEND ?? 'https://localhost:8443';
  const virkailijaUrl =
    env.VITE_VIRKAILIJA_URL ?? 'https://virkailija.testiopintopolku.fi';

  return {
    base: '/ovara/',
    plugins: [react()],
    build: {
      outDir: '../ovara-backend/src/main/resources/public/ovara',
      emptyOutDir: true,
    },
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    server: {
      port: 3405,
      https: readHttpsConfig(),
      proxy: {
        '/ovara-backend': {
          target: ovaraBackend,
          changeOrigin: true,
          secure: false,
        },
        '/lokalisointi': {
          target: virkailijaUrl,
          changeOrigin: true,
          secure: false,
        },
        '/virkailija-raamit': {
          target: virkailijaUrl,
          changeOrigin: true,
          secure: false,
        },
      },
    },
  };
});
