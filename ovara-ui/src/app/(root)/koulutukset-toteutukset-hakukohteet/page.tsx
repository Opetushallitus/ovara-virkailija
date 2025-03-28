'use client';
import { OphTypography } from '@opetushallitus/oph-design-system';
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
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { hasOvaraToinenAsteRole } from '@/app/lib/utils';
import { useState } from 'react';
import { SpinnerModal } from '@/app/components/form/spinner-modal';
import { downloadExcel } from '@/app/components/form/utils';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { MainContainer } from '@/app/components/main-container';

export default function KoulutuksetToteutuksetHakukohteet() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasToinenAsteRights = hasOvaraToinenAsteRole(user?.authorities);
  const { selectedAlkamiskaudet, selectedHaut } = useCommonSearchParams();

  const [isLoading, setIsLoading] = useState(false);
  const queryParamsStr = useSearchParams().toString();

  return (
    <MainContainer>
      {hasToinenAsteRights ? (
        <FormBox>
          {isLoading && <SpinnerModal open={isLoading} />}
          <OphTypography>{t('yleinen.pakolliset-kentat')}</OphTypography>
          <KoulutuksenAlkaminen />
          <Haku haunTyyppi="toinen_aste" />
          <OrganisaatioValikot />
          <KoulutuksenTila />
          <ToteutuksenTila />
          <HakukohteenTila />
          <Valintakoe />
          <Divider />
          <FormButtons
            disabled={!selectedAlkamiskaudet || !selectedHaut}
            downloadExcel={() =>
              downloadExcel(
                'koulutukset-toteutukset-hakukohteet',
                queryParamsStr,
                setIsLoading,
              )
            }
          />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
