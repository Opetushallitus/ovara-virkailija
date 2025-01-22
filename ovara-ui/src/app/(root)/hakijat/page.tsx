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
import { configuration } from '@/app/lib/configuration';
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

export default function Hakijat() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasToinenAsteRights = hasOvaraToinenAsteRole(user?.authorities);
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';
  const queryParams = useSearchParams();
  const organisaatiot = useFetchOrganisaatiohierarkiat();

  return (
    <MainContainer>
      {hasToinenAsteRights ? (
        <FormBox>
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
          <Vastaanottotieto t={t} />
          <Markkinointilupa t={t} />
          <Julkaisulupa t={t} />
          <FormButtons
            disabled={false}
            excelDownloadUrl={
              `${configuration.ovaraBackendApiUrl}/hakijat?` + queryParams
            }
          />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
