'use client';

import { ErrorView } from '@/app/components/error-view';
import { THEME_OVERRIDES } from '@/app/theme';
import { OphNextJsThemeProvider } from '@opetushallitus/oph-design-system/next/theme';
import { OvaraTolgeeProvider } from '@/app/components/ovara-tolgee-provider';
import { MainContainer } from '@/app/components/main-container';
import Header from '@/app/components/header';
import { PageLayout } from '@/app/components/page-layout';
import { PageContent } from '@/app/components/page-content';

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <html lang="fi">
      <body>
        <OphNextJsThemeProvider variant="oph" overrides={THEME_OVERRIDES}>
          <OvaraTolgeeProvider language="fi">
            <PageLayout>
              <Header />
              <PageContent>
                <MainContainer>
                  <ErrorView error={error} reset={reset} />
                </MainContainer>
              </PageContent>
            </PageLayout>
          </OvaraTolgeeProvider>
        </OphNextJsThemeProvider>
      </body>
    </html>
  );
}
