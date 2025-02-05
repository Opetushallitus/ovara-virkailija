'use client';
import { OphTypography } from '@opetushallitus/oph-design-system';
import { MainContainer } from '@/app/components/main-container';
import { FormBox } from '@/app/components/form/form-box';
import { FormButtons } from '@/app/components/form/form-buttons';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { hasOvaraToinenAsteRole } from '@/app/lib/utils';
import { useSearchParams } from 'next/navigation';
import { configuration } from '@/app/lib/configuration';
import { LanguageCode } from '@/app/lib/types/common';

import { KoulutuksenAlkaminen } from '@/app/components/form/koulutuksen-alkaminen';
import { Haku } from '@/app/components/form/haku';
import { OrganisaatioValikot } from '@/app/components/form/organisaatiovalikot';
import { Hakukohde } from '@/app/components/form/hakukohde';
import { Tulostustapa } from '@/app/components/form/tulostustapa';
import { Opetuskieli } from '@/app/components/form/opetuskieli';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';
import { Divider } from '@mui/material';
import { NaytaHakutoiveet } from '@/app/components/form/nayta-hakutoiveet';
import { Sukupuoli } from '@/app/components/form/sukupuoli';

export default function Hakutilasto() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasToinenAsteRights = hasOvaraToinenAsteRole(user?.authorities);
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';
  const queryParams = useSearchParams();

  const isDisabled = true;

  const {
    setSelectedAlkamiskaudet,
    setSelectedHaut,
    setSelectedOppilaitokset,
    setSelectedToimipisteet,
    setSelectedHakukohteet,
  } = useCommonSearchParams();
  const {
    setSelectedTulostustapa,
    setSelectedOpetuskieli,
    setSelectedSukupuoli,
  } = useHakeneetSearchParams();
  return (
    <MainContainer>
      {hasToinenAsteRights ? (
        <FormBox>
          <OphTypography>{t('yleinen.pakolliset-kentat')}</OphTypography>
          <KoulutuksenAlkaminen />
          <Haku />
          <Tulostustapa />
          <OrganisaatioValikot />
          <Hakukohde locale={locale} t={t} />
          <Opetuskieli />
          <Divider />
          <NaytaHakutoiveet t={t} />
          <Sukupuoli t={t} />
          <FormButtons
            disabled={isDisabled}
            excelDownloadUrl={
              `${configuration.ovaraBackendApiUrl}/hakijat?` + queryParams
            }
            fieldsToClear={[
              () => setSelectedAlkamiskaudet(null),
              () => setSelectedHaut(null),
              () => setSelectedOppilaitokset(null),
              () => setSelectedToimipisteet(null),
              () => setSelectedTulostustapa(null),
              () => setSelectedOpetuskieli(null),
              () => setSelectedHakukohteet(null),
              () => setSelectedSukupuoli(null),
            ]}
          />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
