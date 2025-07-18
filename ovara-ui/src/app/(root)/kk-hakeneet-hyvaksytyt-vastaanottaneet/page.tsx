'use client';
import { OphTypography } from '@opetushallitus/oph-design-system';
import { MainContainer } from '@/app/components/main-container';
import { FormBox } from '@/app/components/form/form-box';
import { FormButtons } from '@/app/components/form/form-buttons';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';
import { hasOvaraKkRole, isNullishOrEmpty } from '@/app/lib/utils';
import { useSearchParams } from 'next/navigation';

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
import { SpinnerModal } from '@/app/components/form/spinner-modal';
import { Kansalaisuus } from '@/app/components/form/kansalaisuus';
import { KkTutkinnonTaso } from '@/app/components/form/kk-tutkinnon-taso';
import { Aidinkieli } from '@/app/components/form/aidinkieli';
import { OkmOhjauksenAlat } from '@/app/components/form/okm-ohjauksen-ala';
import { Ensikertalainen } from '@/app/components/form/ensikertalainen';
import { Hakukohderyhma } from '@/app/components/form/hakukohderyhma';
import { useDownloadWithErrorBoundary } from '@/app/hooks/useDownloadWithErrorBoundary';
import { isNullish } from 'remeda';

export default function KkHakutilasto() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasKkRights = hasOvaraKkRole(user?.authorities);
  const queryParams = useSearchParams();

  const { selectedAlkamiskaudet, selectedHaut } = useCommonSearchParams();
  const { selectedTulostustapa } = useHakeneetSearchParams();
  const isDisabled =
    isNullishOrEmpty(selectedAlkamiskaudet) ||
    isNullishOrEmpty(selectedHaut) ||
    isNullish(selectedTulostustapa);

  const tulostustavat = [
    'koulutustoimijoittain',
    'oppilaitoksittain',
    'toimipisteittain',
    'okm-ohjauksen-aloittain',
    'hauittain',
    'hakukohteittain',
    'hakukohderyhmittain',
    'kansalaisuuksittain',
  ];

  const hakukohdeFetchEnabled = !isNullishOrEmpty(selectedHaut);

  const { run, isLoading } = useDownloadWithErrorBoundary();
  const queryParamsStr = queryParams.toString();
  const handleDownload = () =>
    run(() =>
      downloadExcel('kk-hakeneet-hyvaksytyt-vastaanottaneet', queryParamsStr),
    );

  return (
    <MainContainer>
      {hasKkRights ? (
        <FormBox>
          {isLoading && <SpinnerModal open={isLoading} />}
          <OphTypography>{t('yleinen.pakolliset-kentat')}</OphTypography>
          <KoulutuksenAlkaminen />
          <Haku haunTyyppi={'korkeakoulu'} />
          <Tulostustapa tulostustavat={tulostustavat} />
          <OrganisaatioValikot />
          <Hakukohderyhma />
          <Hakukohde
            fetchEnabled={hakukohdeFetchEnabled}
            includeKoulutustoimija={true}
            includeHakukohderyhma={true}
          />
          <OkmOhjauksenAlat />
          <Divider />
          <KkTutkinnonTaso />
          <Aidinkieli />
          <Kansalaisuus />
          <Sukupuoli />
          <Ensikertalainen />
          <NaytaHakutoiveet />
          <FormButtons disabled={isDisabled} downloadExcel={handleDownload} />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
