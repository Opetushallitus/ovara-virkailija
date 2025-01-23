import { AppRouterCacheProvider } from '@mui/material-nextjs/v14-appRouter';
import { OphNextJsThemeProvider } from '@opetushallitus/oph-design-system/next/theme';
import type { Metadata } from 'next';
import Script from 'next/script';
import { configuration } from './lib/configuration';
import { NuqsAdapter } from 'nuqs/adapters/next/app';
import { THEME_OVERRIDES } from '@/app/theme';
import { AuthorizedUserProvider } from './contexts/AuthorizedUserProvider';
import LocalizationProvider from './components/localization-provider';
import ReactQueryClientProvider from '@/app/components/react-query-client-provider';

export const metadata: Metadata = {
  title: 'Opiskelijavalinnan raportointi',
  description: 'Opiskelijavalinnan raportointikäyttöliittymä',
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html>
      <Script src={configuration.raamitUrl} />
      <body>
        <AppRouterCacheProvider>
          <OphNextJsThemeProvider variant="oph" overrides={THEME_OVERRIDES}>
            <ReactQueryClientProvider>
              <AuthorizedUserProvider>
                <LocalizationProvider>
                  <NuqsAdapter>{children}</NuqsAdapter>
                </LocalizationProvider>
              </AuthorizedUserProvider>
            </ReactQueryClientProvider>
          </OphNextJsThemeProvider>
        </AppRouterCacheProvider>
      </body>
    </html>
  );
}
