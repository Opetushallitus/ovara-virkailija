import { AppRouterCacheProvider } from '@mui/material-nextjs/v14-appRouter';
import { OphNextJsThemeProvider } from '@opetushallitus/oph-design-system/next/theme';
import type { Metadata } from 'next';
import Script from 'next/script';
import { configuration } from './lib/configuration/configuration';
import { NuqsAdapter } from 'nuqs/adapters/next/app';
import { THEME_OVERRIDES } from '@/app/theme';
import { AuthorizedUserProvider } from './contexts/AuthorizedUserProvider';
import LocalizationProvider from './components/providers/localization-provider';
import ReactQueryClientProvider from '@/app/components/providers/react-query-client-provider';
import { ClientErrorBoundary } from '@/app/components/client-errorboundary';
import { ConfigurationProvider } from './components/providers/configuration-provider';

export const metadata: Metadata = {
  title: 'Opiskelijavalinnan raportointi',
  description: 'Opiskelijavalinnan raportointikäyttöliittymä',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="fi">
      <Script src={configuration.raamitUrl} />
      <body>
        <AppRouterCacheProvider>
          <OphNextJsThemeProvider variant="oph" overrides={THEME_OVERRIDES}>
            <ConfigurationProvider configuration={configuration}>
              <ReactQueryClientProvider>
                <AuthorizedUserProvider>
                  <LocalizationProvider>
                    <ClientErrorBoundary>
                      <NuqsAdapter>{children}</NuqsAdapter>
                    </ClientErrorBoundary>
                  </LocalizationProvider>
                </AuthorizedUserProvider>
              </ReactQueryClientProvider>
            </ConfigurationProvider>
          </OphNextJsThemeProvider>
        </AppRouterCacheProvider>
      </body>
    </html>
  );
}
