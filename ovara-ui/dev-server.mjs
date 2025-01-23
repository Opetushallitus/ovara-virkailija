import next from 'next';
import { parse } from 'url';
import { createServer } from 'https';
import fs from 'fs';
import { createProxyMiddleware } from 'http-proxy-middleware';
import nextConfig from './next.config.mjs';

const basePath = nextConfig.basePath;
const port = parseInt(process.env.PORT, 10) || 3405;

const virkailijaOrigin = process.env.VIRKAILIJA_URL;
const ovaraBackendOrigin = process.env.OVARA_BACKEND;
const isProd = process.env.NODE_ENV === 'production';

const app = next({
  conf: nextConfig,
  dev: !isProd,
  hostname: 'localhost',
  port: port,
  env: process.env,
  experimentalHttpsServer: true,
});

const handle = app.getRequestHandler();

const proxy = (origin) =>
  createProxyMiddleware({
    autoRewrite: true,
    headers: {
      'Access-Control-Allow-Origin': '*',
    },
    changeOrigin: true,
    cookieDomainRewrite: 'localhost',
    secure: false,
    target: origin,
  });

const ovaraBackendProxy = proxy(ovaraBackendOrigin);
const virkailijaProxy = proxy(virkailijaOrigin);

const httpsOptions = {
  key: fs.readFileSync('./certificates/localhost-key.pem'),
  cert: fs.readFileSync('./certificates/localhost.pem'),
};

app.prepare().then(() => {
  createServer(httpsOptions, (req, res) => {
    const parsedUrl = parse(req.url, true);
    const { pathname } = parsedUrl;
    if (!pathname || pathname === '' || pathname === '/') {
      res.writeHead(302, { Location: basePath });
      res.end();
    } else if (ovaraBackendOrigin && pathname.startsWith('/ovara-backend')) {
      ovaraBackendProxy(req, res);
    } else if (pathname.startsWith(basePath)) {
      handle(req, res, parsedUrl);
    } else {
      virkailijaProxy(req, res);
    }
  })
    .listen(port, () => {
      console.log('ready - started server on url: https://localhost:' + port);
    })
    .on('error', (e) => {
      console.error(e);
    });
});
