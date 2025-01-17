'use client';
import { OphTypography } from '@opetushallitus/oph-design-system';
import { MainContainer } from '@/app/components/main-container';
import { FormBox } from '@/app/components/form/form-box';
import { FormButtons } from '@/app/components/form/form-buttons';
import { Divider } from '@mui/material';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { hasOvaraToinenAsteRole } from '@/app/lib/utils';
import { useSearchParams } from 'next/navigation';
import { configuration } from '@/app/lib/configuration';
import { useFetchOrganisaatiohierarkiat } from '@/app/hooks/useFetchOrganisaatiohierarkiat';

import { KoulutuksenAlkaminen } from '@/app/components/form/koulutuksen-alkaminen';
import { Haku } from '@/app/components/form/haku';

export default function Hakijat() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasToinenAsteRights = hasOvaraToinenAsteRole(user?.authorities);
  const queryParams = useSearchParams();
  const organisaatiot = useFetchOrganisaatiohierarkiat();
  console.log({ organisaatiot });

  return (
    <MainContainer>
      {hasToinenAsteRights ? (
        <FormBox>
          <OphTypography>{t('yleinen.pakolliset-kentat')}</OphTypography>
          <KoulutuksenAlkaminen />
          <Haku />
          <Divider />
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
