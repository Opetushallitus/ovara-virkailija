'use client';
import { OphTypography } from '@opetushallitus/oph-design-system';
import { MainContainer } from '@/app/components/main-container';
import { FormBox } from '@/app/components/form/form-box';
import { FormButtons } from '@/app/components/form/form-buttons';
import { Box, Divider } from '@mui/material';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { hasOvaraToinenAsteRole } from '@/app/lib/utils';
import { useSearchParams } from 'next/navigation';
import { LanguageCode } from '@/app/lib/types/common';
import { useFetchOrganisaatiohierarkiat } from '@/app/hooks/useFetchOrganisaatiohierarkiat';

import { KoulutuksenAlkaminen } from '@/app/components/form/koulutuksen-alkaminen';
import { Haku } from '@/app/components/form/haku';
import {
  OppilaitosValikko,
  ToimipisteValikko,
} from '@/app/components/form/organisaatiovalikot';
import { Hakukohde } from '@/app/components/form/hakukohde';
import { Vastaanottotieto } from '@/app/components/form/vastaanottotieto';
import { Markkinointilupa } from '@/app/components/form/markkinointilupa';
import { Julkaisulupa } from '@/app/components/form/julkaisulupa';
import { Harkinnanvaraisuus } from '@/app/components/form/harkinnanvaraisuus';
import { useState } from 'react';
import { SpinnerModal } from '@/app/components/form/spinner-modal';
import { downloadExcel } from '@/app/components/form/utils';
import { Valintatieto } from '@/app/components/form/valintatieto';
import { Kaksoistutkinto } from '@/app/components/form/kaksoistutkinto';
import { SoraTerveys } from '@/app/components/form/soraTerveys';
import { SoraAiempi } from '@/app/components/form/soraAiempi';
import { Urheilijatutkinto } from '@/app/components/form/Urheilijatutkinto';
import { Pohjakoulutus } from '@/app/components/form/pohjakoulutus';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';

export default function Hakijat() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasToinenAsteRights = hasOvaraToinenAsteRole(user?.authorities);
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';
  const organisaatiot = useFetchOrganisaatiohierarkiat().data;
  const {
    selectedAlkamiskaudet,
    setSelectedAlkamiskaudet,
    selectedHaut,
    setSelectedHaut,
    selectedOppilaitokset,
    selectedToimipisteet,
    setSelectedOppilaitokset,
    setSelectedToimipisteet,
    setSelectedHakukohteet,
    setSelectedHarkinnanvaraisuus,
  } = useCommonSearchParams();
  const {
    setSelectedJulkaisulupa,
    setSelectedMarkkinointilupa,
    setSelectedVastaanottotieto,
  } = useHakijatSearchParams();

  const isDisabled = !(
    selectedAlkamiskaudet &&
    selectedHaut &&
    (selectedOppilaitokset || selectedToimipisteet)
  );
  const [isLoading, setIsLoading] = useState(false);
  const queryParamsStr = useSearchParams().toString();

  return (
    <MainContainer>
      {hasToinenAsteRights ? (
        <FormBox>
          {isLoading && <SpinnerModal open={isLoading} />}
          <OphTypography>{t('yleinen.pakolliset-kentat')}</OphTypography>
          <KoulutuksenAlkaminen />
          <Haku haunTyyppi={'toinen_aste'} />
          <Divider />
          <OphTypography>
            {t('raportti.oppilaitos-tai-toimipiste')}
          </OphTypography>
          <Box>
            <OppilaitosValikko
              locale={locale}
              organisaatiot={organisaatiot}
              t={t}
            />
            <ToimipisteValikko
              locale={locale}
              organisaatiot={organisaatiot}
              t={t}
            />
          </Box>
          <Divider />
          <Hakukohde locale={locale} t={t} sx={{ paddingTop: 0 }} />
          <Pohjakoulutus locale={locale} t={t} />
          <Divider />
          <Valintatieto t={t} />
          <Vastaanottotieto t={t} />
          <Harkinnanvaraisuus t={t} />
          <Kaksoistutkinto t={t} />
          <Urheilijatutkinto t={t} />
          <SoraTerveys t={t} />
          <SoraAiempi t={t} />
          <Markkinointilupa t={t} />
          <Julkaisulupa t={t} />
          <FormButtons
            disabled={isDisabled}
            downloadExcel={() =>
              downloadExcel('hakijat', queryParamsStr, setIsLoading)
            }
            fieldsToClear={[
              () => setSelectedAlkamiskaudet(null),
              () => setSelectedHaut(null),
              () => setSelectedOppilaitokset(null),
              () => setSelectedToimipisteet(null),
              () => setSelectedHakukohteet(null),
              () => setSelectedVastaanottotieto(null),
              () => setSelectedMarkkinointilupa(null),
              () => setSelectedJulkaisulupa(null),
              () => setSelectedHarkinnanvaraisuus(null),
            ]}
          />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
