import { BrowserRouter, Route, Routes } from 'react-router';
import { NuqsAdapter } from 'nuqs/adapters/react-router/v7';
import { OphThemeProvider } from '@opetushallitus/oph-design-system/theme';
import { configuration } from '@/app/lib/configuration/configuration';
import { THEME_OVERRIDES } from '@/app/theme';
import { ConfigurationProvider } from '@/app/components/providers/configuration-provider';
import ReactQueryClientProvider from '@/app/components/providers/react-query-client-provider';
import { AuthorizedUserProvider } from '@/app/components/providers/authorized-user-provider';
import LocalizationProvider from '@/app/components/providers/localization-provider';
import { ClientErrorBoundary } from '@/app/components/client-errorboundary';
import Header from '@/app/components/header';
import { PageLayout } from '@/app/components/page-layout';
import Home from '@/app/(root)/page';
import HakeneetHyvaksytytVastaanottaneet from '@/app/(root)/hakeneet-hyvaksytyt-vastaanottaneet/page';
import Hakijat from '@/app/(root)/hakijat/page';
import KkHakeneetHyvaksytytVastaanottaneet from '@/app/(root)/kk-hakeneet-hyvaksytyt-vastaanottaneet/page';
import KkHakijat from '@/app/(root)/kk-hakijat/page';
import KkKoulutuksetToteutuksetHakukohteet from '@/app/(root)/kk-koulutukset-toteutukset-hakukohteet/page';
import KoulutuksetToteutuksetHakukohteet from '@/app/(root)/koulutukset-toteutukset-hakukohteet/page';
import NotFound from '@/app/not-found';

function RaamitScript() {
  return <script src={configuration.raamitUrl} />;
}

export default function App() {
  return (
    <>
      <RaamitScript />
      <OphThemeProvider variant="oph" overrides={THEME_OVERRIDES}>
        <ConfigurationProvider configuration={configuration}>
          <ReactQueryClientProvider>
            <AuthorizedUserProvider>
              <LocalizationProvider>
                <ClientErrorBoundary>
                  <NuqsAdapter>
                    <BrowserRouter basename="/ovara">
                      <PageLayout>
                        <Header />
                        <Routes>
                          <Route index element={<Home />} />
                          <Route path="hakijat" element={<Hakijat />} />
                          <Route
                            path="hakeneet-hyvaksytyt-vastaanottaneet"
                            element={<HakeneetHyvaksytytVastaanottaneet />}
                          />
                          <Route path="kk-hakijat" element={<KkHakijat />} />
                          <Route
                            path="kk-hakeneet-hyvaksytyt-vastaanottaneet"
                            element={<KkHakeneetHyvaksytytVastaanottaneet />}
                          />
                          <Route
                            path="kk-koulutukset-toteutukset-hakukohteet"
                            element={<KkKoulutuksetToteutuksetHakukohteet />}
                          />
                          <Route
                            path="koulutukset-toteutukset-hakukohteet"
                            element={<KoulutuksetToteutuksetHakukohteet />}
                          />
                          <Route path="*" element={<NotFound />} />
                        </Routes>
                      </PageLayout>
                    </BrowserRouter>
                  </NuqsAdapter>
                </ClientErrorBoundary>
              </LocalizationProvider>
            </AuthorizedUserProvider>
          </ReactQueryClientProvider>
        </ConfigurationProvider>
      </OphThemeProvider>
    </>
  );
}
