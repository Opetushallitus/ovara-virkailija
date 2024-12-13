'use client';
import { OphTypography, ophColors } from '@opetushallitus/oph-design-system';
import { MainContainer } from '@/app/components/main-container';
import { useTranslations } from 'next-intl';

import { KoulutuksenAlkaminen } from '@/app/components/form/koulutuksen-alkaminen';
import { Haku } from '@/app/components/form/haku';
import {
  HakukohteenTila,
  KoulutuksenTila,
  ToteutuksenTila,
} from '@/app/components/form/tila';
import { OrganisaatioValikot } from '@/app/components/form/organisaatiovalikot';
import { Valintakoe } from '@/app/components/form/valintakoe';
import { FormButtons } from '@/app/components/form/form-buttons';
import { Divider, styled } from '@mui/material';
import { useSearchParams } from 'next/navigation';
import { configuration } from '@/app/lib/configuration';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { hasOvaraToinenAsteRole } from '@/app/lib/utils';

const FormBox = styled('form')(({ theme }) => ({
  border: `1px solid ${ophColors.grey100}`,
  padding: theme.spacing(2.5),
  width: '100%',
}));

export default function KoulutuksetToteutuksetHakukohteet() {
  const t = useTranslations();
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
