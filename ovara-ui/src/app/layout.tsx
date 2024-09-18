import { AppRouterCacheProvider } from '@mui/material-nextjs/v14-appRouter';
import { OphNextJsThemeProvider } from '@opetushallitus/oph-design-system/next/theme';
import type { Metadata } from 'next';
import { NextIntlClientProvider } from 'next-intl';
import { getLocale, getMessages } from 'next-intl/server';
import { type OphLanguage } from '@opetushallitus/oph-design-system';

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
      <body>
        <AppRouterCacheProvider>
          <NextIntlClientProvider messages={messages}>
            <OphNextJsThemeProvider variant="oph" lang={locale}>
              {children}
            </OphNextJsThemeProvider>
          </NextIntlClientProvider>
        </AppRouterCacheProvider>
      </body>
    </html>
  );
}
