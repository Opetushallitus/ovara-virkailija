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

  const { selectedAlkamiskaudet, selectedHaut } = useCommonSearchParams();
  const { selectedTulostustapa } = useHakeneetSearchParams();

  const isDisabled = !(
    selectedAlkamiskaudet &&
    selectedHaut &&
    selectedTulostustapa
  );

  const tulostustavat = [
    'koulutustoimijoittain',
    'oppilaitoksittain',
    'toimipisteittain',
    'koulutusaloittain',
    'hakukohteittain',
  ];

  const [isLoading, setIsLoading] = useState(false);
  const queryParamsStr = queryParams.toString();
  return (
    <MainContainer>
      {hasToinenAsteRights ? (
        <FormBox>
          {isLoading && <SpinnerModal open={isLoading} />}
          <OphTypography>{t('yleinen.pakolliset-kentat')}</OphTypography>
          <KoulutuksenAlkaminen />
          <Haku haunTyyppi={'toinen_aste'} />
          <Tulostustapa tulostustavat={tulostustavat} />
          <OrganisaatioValikot />
          <Hakukohde locale={locale} t={t} />
          <Opetuskieli />
          <KoulutusalaValikot />
          <MaakuntaKuntaValikot />
          <Harkinnanvaraisuus t={t} />
          <Divider />
          <Sukupuoli />
          <NaytaHakutoiveet />
          <FormButtons
            disabled={isDisabled}
            downloadExcel={() =>
              downloadExcel(
                'hakeneet-hyvaksytyt-vastaanottaneet',
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
