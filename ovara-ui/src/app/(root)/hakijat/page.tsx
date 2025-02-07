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
import { useSearchParams as useQueryParams } from 'next/navigation';
import { useState } from 'react';
import { SpinnerModal } from '@/app/components/form/spinner-modal';
import { downloadExcel } from '@/app/components/form/utils';
import { Valintatieto } from '@/app/components/form/valintatieto';
import { Kaksoistutkinto } from '@/app/components/form/kaksoistutkinto';

export default function Hakijat() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasToinenAsteRights = hasOvaraToinenAsteRole(user?.authorities);
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';
  const queryParams = useSearchParams();
  const organisaatiot = useFetchOrganisaatiohierarkiat();
  const alkamiskausi = queryParams.get('alkamiskausi');
  const haku = queryParams.get('haku');
  const oppilaitos = queryParams.get('oppilaitos');
  const toimipiste = queryParams.get('toimipiste');

  const isDisabled = !(alkamiskausi && haku && (oppilaitos || toimipiste));

  const [isLoading, setIsLoading] = useState(false);
  const queryParamsStr = useQueryParams().toString();

  return (
    <MainContainer>
      {hasToinenAsteRights ? (
        <FormBox>
          {isLoading && <SpinnerModal open={isLoading} />}
          <OphTypography>{t('yleinen.pakolliset-kentat')}</OphTypography>
          <KoulutuksenAlkaminen />
          <Haku />
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
          <Hakukohde locale={locale} t={t} />
          <Divider />
          <Valintatieto t={t} />
          <Vastaanottotieto t={t} />
          <Harkinnanvaraisuus t={t} />
          <Kaksoistutkinto t={t} />
          <Markkinointilupa t={t} />
          <Julkaisulupa t={t} />
          <FormButtons
            disabled={isDisabled}
            downloadExcel={() =>
              downloadExcel('hakijat', queryParamsStr, setIsLoading)
            }
          />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
