'use client';
import { OphTypography } from '@opetushallitus/oph-design-system';
import { FormBox } from '@/app/components/form/form-box';
import { FormButtons } from '@/app/components/form/form-buttons';
import { Box, Divider } from '@mui/material';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { hasOvaraToinenAsteRole, isNullishOrEmpty } from '@/app/lib/utils';
import { useSearchParams } from 'next/navigation';
import { useFetchOrganisaatiohierarkiat } from '@/app/hooks/useFetchOrganisaatiohierarkiat';

import { KoulutuksenAlkaminen } from '@/app/components/form/koulutuksen-alkaminen';
import { Haku } from '@/app/components/form/haku';
import {
  Oppilaitos,
  Toimipiste,
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
import { MainContainer } from '@/app/components/main-container';

export default function Hakijat() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasToinenAsteRights = hasOvaraToinenAsteRole(user?.authorities);
  const organisaatiot = useFetchOrganisaatiohierarkiat().data;
  const { selectedHaut, selectedOppilaitokset, selectedToimipisteet } =
    useCommonSearchParams();

  const [isLoading, setIsLoading] = useState(false);
  const queryParamsStr = useSearchParams().toString();

  const fetchEnabled =
    !isNullishOrEmpty(selectedHaut) &&
    ![selectedOppilaitokset, selectedToimipisteet].every(isNullishOrEmpty);

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
            <Oppilaitos organisaatiot={organisaatiot} />
            <Toimipiste organisaatiot={organisaatiot} />
          </Box>
          <Divider />
          <Hakukohde sx={{ marginTop: 0 }} fetchEnabled={fetchEnabled} />
          <Pohjakoulutus />
          <Valintatieto sx={{ paddingTop: 0 }} />
          <Vastaanottotieto />
          <Harkinnanvaraisuus />
          <Kaksoistutkinto />
          <Urheilijatutkinto />
          <SoraTerveys />
          <SoraAiempi />
          <Markkinointilupa />
          <Julkaisulupa />
          <FormButtons
            disabled={!fetchEnabled}
            downloadExcel={() =>
              downloadExcel('hakijat', queryParamsStr, setIsLoading)
            }
          />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
