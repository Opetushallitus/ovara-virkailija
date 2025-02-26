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
import { KoulutusalaValikot } from '@/app/components/form/koulutusalavalikot';
import { MaakuntaKuntaValikot } from '@/app/components/form/maakunta-kunta';

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
    setSelectedKoulutusalat1,
    setSelectedKoulutusalat2,
    setSelectedKoulutusalat3,
    setSelectedMaakunnat,
    setSelectedKunnat,
    setSelectedNaytaHakutoiveet,
    selectedTulostustapa,
    setSelectedTulostustapa,
    setSelectedOpetuskielet,
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
          <KoulutusalaValikot />
          <MaakuntaKuntaValikot />
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
              () => setSelectedKoulutusalat1(null),
              () => setSelectedKoulutusalat2(null),
              () => setSelectedKoulutusalat3(null),
              () => setSelectedMaakunnat(null),
              () => setSelectedKunnat(null),
              () => setSelectedNaytaHakutoiveet(null),
              () => setSelectedOpetuskielet(null),
              () => setSelectedHakukohteet(null),
              () => setSelectedSukupuoli(null),
            ]}
          />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
