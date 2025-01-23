const cspHeader = `
    default-src 'self';
    script-src 'self' 'unsafe-eval' 'unsafe-inline';
    style-src 'self' 'unsafe-inline';
    img-src 'self' blob: data:;
    font-src 'self';
    object-src 'none';
    base-uri 'self';
    form-action 'self';
    frame-ancestors 'none';
    block-all-mixed-content;
    upgrade-insecure-requests;
`;

const isStandalone = process.env.STANDALONE === 'true';

const basePath = '/ovara';

/** @type {import('next').NextConfig} */
export const nextConfig = {
  basePath,
  compress: false, // nginx hoitaa pakkauksen
  eslint: {
    ignoreDuringBuilds: true,
  },
  typescript: {
    ignoreBuildErrors: Boolean(process.env.SKIP_TYPECHECK),
  },
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          {
            key: 'Content-Security-Policy',
            value: cspHeader.replace(/\n/g, ''),
          },
        ],
      },
    ];
  },
  env: {
    VIRKAILIJA_URL: process.env.VIRKAILIJA_URL,
    APP_URL: process.env.APP_URL,
    OVARA_BACKEND: process.env.APP_URL ?? process.env.VIRKAILIJA_URL,
    DEV: process.env.DEV,
  },
  output: isStandalone ? 'standalone' : undefined,
};

export default nextConfig;
