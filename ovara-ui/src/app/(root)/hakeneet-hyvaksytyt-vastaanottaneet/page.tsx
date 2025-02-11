'use client';
import { OphTypography } from '@opetushallitus/oph-design-system';
import { MainContainer } from '@/app/components/main-container';
import { FormBox } from '@/app/components/form/form-box';
import { FormButtons } from '@/app/components/form/form-buttons';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { hasOvaraToinenAsteRole } from '@/app/lib/utils';
import { useSearchParams } from 'next/navigation';
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
import { Harkinnanvaraisuus } from '@/app/components/form/harkinnanvaraisuus';
import { downloadExcel } from '@/app/components/form/utils';
import { useState } from 'react';
import { SpinnerModal } from '@/app/components/form/spinner-modal';

export default function Hakutilasto() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasToinenAsteRights = hasOvaraToinenAsteRole(user?.authorities);
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';
  const queryParams = useSearchParams();

  const {
    selectedAlkamiskaudet,
    setSelectedAlkamiskaudet,
    selectedHaut,
    setSelectedHaut,
    setSelectedOppilaitokset,
    setSelectedToimipisteet,
    setSelectedHakukohteet,
  } = useCommonSearchParams();
  const {
    selectedTulostustapa,
    setSelectedTulostustapa,
    setSelectedOpetuskieli,
    setSelectedSukupuoli,
  } = useHakeneetSearchParams();

  const isDisabled = !(
    selectedAlkamiskaudet &&
    selectedHaut &&
    selectedTulostustapa
  );
  const [isLoading, setIsLoading] = useState(false);
  const queryParamsStr = queryParams.toString();
  return (
    <MainContainer>
      {hasToinenAsteRights ? (
        <FormBox>
          {isLoading && <SpinnerModal open={isLoading} />}
          <OphTypography>{t('yleinen.pakolliset-kentat')}</OphTypography>
          <KoulutuksenAlkaminen />
          <Haku />
          <Tulostustapa />
          <OrganisaatioValikot />
          <Hakukohde locale={locale} t={t} />
          <Opetuskieli />
          <Harkinnanvaraisuus t={t} />
          <Divider />
          <NaytaHakutoiveet t={t} />
          <Sukupuoli t={t} />
          <FormButtons
            disabled={isDisabled}
            downloadExcel={() =>
              downloadExcel(
                'hakeneet-hyvaksytyt-vastaanottaneet',
                queryParamsStr,
                setIsLoading,
              )
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
