import { AppRouterCacheProvider } from '@mui/material-nextjs/v14-appRouter';
import { OphNextJsThemeProvider } from '@opetushallitus/oph-design-system/next/theme';
import type { Metadata } from 'next';
import Script from 'next/script';
import { configuration } from './lib/configuration';
import { NuqsAdapter } from 'nuqs/adapters/next/app';
import { THEME_OVERRIDES } from '@/app/theme';
import { AuthorizedUserProvider } from './contexts/AuthorizedUserProvider';
import LocalizationProvider from './components/localization-provider';
import { getLocale, getMessages } from 'next-intl/server';
import { LanguageCode } from './lib/types/common';

export const metadata: Metadata = {
  title: 'Opiskelijavalinnan raportointi',
  description: 'Opiskelijavalinnan raportointikäyttöliittymä',
};

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const locale = (await getLocale()) as LanguageCode;
  const messages = (await getMessages()) as IntlMessages;

  return (
    <html lang={locale}>
      <Script src={configuration.raamitUrl} />
      <body>
        <AppRouterCacheProvider>
          <OphNextJsThemeProvider variant="oph" overrides={THEME_OVERRIDES}>
            <AuthorizedUserProvider>
              <LocalizationProvider messagesFromLocalFile={messages}>
                <NuqsAdapter>{children}</NuqsAdapter>
              </LocalizationProvider>
            </AuthorizedUserProvider>
          </OphNextJsThemeProvider>
        </AppRouterCacheProvider>
      </body>
    </html>
  );
}
