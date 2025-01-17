'use client';
import { OphTypography } from '@opetushallitus/oph-design-system';
import { MainContainer } from '@/app/components/main-container';
import { useTranslate } from '@tolgee/react';

import { KoulutuksenAlkaminen } from '@/app/components/form/koulutuksen-alkaminen';
import { Haku } from '@/app/components/form/haku';
import {
  HakukohteenTila,
  KoulutuksenTila,
  ToteutuksenTila,
} from '@/app/components/form/tila';
import { OrganisaatioValikot } from '@/app/components/form/organisaatiovalikot';
import { FormBox } from '@/app/components/form/form-box';
import { Valintakoe } from '@/app/components/form/valintakoe';
import { FormButtons } from '@/app/components/form/form-buttons';
import { Divider } from '@mui/material';
import { useSearchParams } from 'next/navigation';
import { configuration } from '@/app/lib/configuration';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { hasOvaraToinenAsteRole } from '@/app/lib/utils';

export default function KoulutuksetToteutuksetHakukohteet() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasToinenAsteRights = hasOvaraToinenAsteRole(user?.authorities);
  const queryParams = useSearchParams();
  const alkamiskausi = queryParams.get('alkamiskausi');
  const haku = queryParams.get('haku');

  return (
    <MainContainer>
      {hasToinenAsteRights ? (
        <FormBox>
          <OphTypography>{t('yleinen.pakolliset-kentat')}</OphTypography>
          <KoulutuksenAlkaminen />
          <Haku />
          <OrganisaatioValikot />
          <KoulutuksenTila />
          <ToteutuksenTila />
          <HakukohteenTila />
          <Valintakoe />
          <Divider />
          <FormButtons
            disabled={!alkamiskausi || !haku}
            excelDownloadUrl={
              `${configuration.ovaraBackendApiUrl}/koulutukset-toteutukset-hakukohteet?` +
              queryParams
            }
          />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
