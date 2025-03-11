'use client';
import { OphTypography } from '@opetushallitus/oph-design-system';
import { MainContainer } from '@/app/components/main-container';
import { FormBox } from '@/app/components/form/form-box';
import { FormButtons } from '@/app/components/form/form-buttons';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { hasOvaraKkRole } from '@/app/lib/utils';
import { useSearchParams } from 'next/navigation';
import { LanguageCode } from '@/app/lib/types/common';

import { KoulutuksenAlkaminen } from '@/app/components/form/koulutuksen-alkaminen';
import { Haku } from '@/app/components/form/haku';
import { OrganisaatioValikot } from '@/app/components/form/organisaatiovalikot';
import { Hakukohde } from '@/app/components/form/hakukohde';
import { Tulostustapa } from '@/app/components/form/tulostustapa';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';
import { Divider } from '@mui/material';
import { NaytaHakutoiveet } from '@/app/components/form/nayta-hakutoiveet';
import { Sukupuoli } from '@/app/components/form/sukupuoli';
import { downloadExcel } from '@/app/components/form/utils';
import { useState } from 'react';
import { SpinnerModal } from '@/app/components/form/spinner-modal';
import { Kansalaisuus } from '@/app/components/form/kansalaisuus';
import { KkTutkinnonTaso } from '@/app/components/form/kk-tutkinnon-taso';
import { Aidinkieli } from '@/app/components/form/aidinkieli';
import { OkmOhjauksenAlat } from '@/app/components/form/okm-ohjauksen-ala';
import { Ensikertalainen } from '@/app/components/form/ensikertalainen';
import { Hakukohderyhma } from '@/app/components/form/hakukohderyhma';

export default function KkHakutilasto() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasKkRights = hasOvaraKkRole(user?.authorities);
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';
  const queryParams = useSearchParams();

  const {
    selectedAlkamiskaudet,
    setSelectedAlkamiskaudet,
    selectedHaut,
    setSelectedHaut,
    setSelectedKoulutustoimija,
    setSelectedOppilaitokset,
    setSelectedToimipisteet,
    setSelectedHakukohteet,
    setSelectedKansalaisuus,
  } = useCommonSearchParams();
  const {
    setSelectedNaytaHakutoiveet,
    selectedTulostustapa,
    setSelectedTulostustapa,
    setSelectedSukupuoli,
    setSelectedTutkinnonTaso,
    setSelectedAidinkieli,
    setSelectedOkmOhjauksenAla,
    setSelectedEnsikertalainen,
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
      {hasKkRights ? (
        <FormBox>
          {isLoading && <SpinnerModal open={isLoading} />}
          <OphTypography>{t('yleinen.pakolliset-kentat')}</OphTypography>
          <KoulutuksenAlkaminen />
          <Haku haunTyyppi={'korkeakoulu'} />
          <Tulostustapa />
          <OrganisaatioValikot />
          <Hakukohderyhma />
          <Hakukohde locale={locale} t={t} />
          <OkmOhjauksenAlat />
          <Divider />
          <KkTutkinnonTaso />
          <Aidinkieli />
          <Kansalaisuus />
          <Sukupuoli />
          <Ensikertalainen />
          <NaytaHakutoiveet />
          <FormButtons
            disabled={isDisabled}
            downloadExcel={() =>
              downloadExcel(
                'kk-hakeneet-hyvaksytyt-vastaanottaneet',
                queryParamsStr,
                setIsLoading,
              )
            }
            fieldsToClear={[
              () => setSelectedAlkamiskaudet(null),
              () => setSelectedHaut(null),
              () => setSelectedKoulutustoimija(null),
              () => setSelectedOppilaitokset(null),
              () => setSelectedToimipisteet(null),
              () => setSelectedTulostustapa(null),
              () => setSelectedNaytaHakutoiveet(null),
              () => setSelectedHakukohteet(null),
              () => setSelectedOkmOhjauksenAla(null),
              () => setSelectedEnsikertalainen(null),
              () => setSelectedTutkinnonTaso(null),
              () => setSelectedAidinkieli(null),
              () => setSelectedKansalaisuus(null),
              () => setSelectedSukupuoli(null),
            ]}
          />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
