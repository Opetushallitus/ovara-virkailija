import react from '@vitejs/plugin-react';
import { defineConfig, loadEnv, type Plugin } from 'vite';
import fs from 'node:fs';
import path from 'node:path';

const trailingSlashRedirectPlugin: Plugin = {
  name: 'slash-redirect',
  configureServer(server) {
    server.middlewares.use((req, res, next) => {
      if (req.url === '/ovara') {
        res.writeHead(302, {
          Location: '/ovara/',
        });
        res.end();
        return;
      }

      next();
    });
  },
};

const readHttpsConfig = () => {
  const certPath = path.resolve('certificates/localhost.pem');
  const keyPath = path.resolve('certificates/localhost-key.pem');

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
    plugins: [trailingSlashRedirectPlugin, react()],
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
