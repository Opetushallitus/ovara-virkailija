import { AppRouterCacheProvider } from '@mui/material-nextjs/v14-appRouter';
import { OphNextJsThemeProvider } from '@opetushallitus/oph-design-system/next/theme';
import type { Metadata } from 'next';
import { NextIntlClientProvider } from 'next-intl';
import { getLocale, getMessages } from 'next-intl/server';
import { type OphLanguage } from '@opetushallitus/oph-design-system';
import Script from 'next/script';
import { configuration } from './lib/configuration';
import PermissionProvider from './components/permission-provider';
import ReactQueryClientProvider from './components/react-query-client-provider';

export const metadata: Metadata = {
  title: 'Opiskelijavalinnan raportointi',
  description: 'Opiskelijavalinnan raportointikäyttöliittymä',
};

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const locale = (await getLocale()) as OphLanguage;
  const messages = await getMessages();

  return (
    <html lang={locale}>
      <Script src={configuration.raamitUrl} />
      <body>
        <AppRouterCacheProvider>
          <NextIntlClientProvider messages={messages}>
            <ReactQueryClientProvider>
              <PermissionProvider>
                <OphNextJsThemeProvider variant="oph" lang={locale}>
                  {children}
                </OphNextJsThemeProvider>
              </PermissionProvider>
            </ReactQueryClientProvider>
          </NextIntlClientProvider>
        </AppRouterCacheProvider>
      </body>
    </html>
  );
}
