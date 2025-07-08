'use client';
import { OphTypography } from '@opetushallitus/oph-design-system';
import { MainContainer } from '@/app/components/main-container';
import { FormBox } from '@/app/components/form/form-box';
import { FormButtons } from '@/app/components/form/form-buttons';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';
import {
  hasOphPaaKayttajaRole,
  hasOvaraToinenAsteRole,
  isNullishOrEmpty,
} from '@/app/lib/utils';
import { useSearchParams } from 'next/navigation';

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
import {
  changeRadioGroupSelection,
  downloadExcel,
  getSelectedRadioGroupValue,
} from '@/app/components/form/utils';
import { SpinnerModal } from '@/app/components/form/spinner-modal';
import { KoulutusalaValikot } from '@/app/components/form/koulutusalavalikot';
import { MaakuntaKuntaValikot } from '@/app/components/form/maakunta-kunta';
import { useDownloadWithErrorBoundary } from '@/app/hooks/useDownloadWithErrorBoundary';
import { isNullish } from 'remeda';
import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { RADIOGROUP_BOOLEAN_OPTIONS } from '@/app/lib/constants';
import { useBooleanQueryStateWithOptions } from '@/app/hooks/searchParams/useQueryStateWithLocalStorage';

export default function Hakutilasto() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasToinenAsteRights = hasOvaraToinenAsteRole(user?.authorities);
  const queryParams = useSearchParams();

  const { selectedAlkamiskaudet, selectedHaut } = useCommonSearchParams();
  const { selectedTulostustapa } = useHakeneetSearchParams();
  const { run, isLoading } = useDownloadWithErrorBoundary();
  const [uusiTilasto, setUusiTilasto] = useBooleanQueryStateWithOptions(
    'ovara_uusi_tilasto',
    true,
  );

  const isDisabled =
    isNullishOrEmpty(selectedAlkamiskaudet) ||
    isNullishOrEmpty(selectedHaut) ||
    isNullish(selectedTulostustapa);

  const tulostustavat = [
    'koulutustoimijoittain',
    'oppilaitoksittain',
    'toimipisteittain',
    'koulutusaloittain',
    'hakukohteittain',
  ];

  const hakukohdeFetchEnabled = !isNullishOrEmpty(selectedHaut);

  const queryParamsStr = queryParams.toString();
  const handleDownload = () =>
    run(() =>
      downloadExcel('hakeneet-hyvaksytyt-vastaanottaneet', queryParamsStr),
    );

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
          <Hakukohde
            fetchEnabled={hakukohdeFetchEnabled}
            includeKoulutustoimija={true}
          />
          <Opetuskieli />
          <KoulutusalaValikot />
          <MaakuntaKuntaValikot />
          <Harkinnanvaraisuus />
          <Divider />
          <Sukupuoli />
          <NaytaHakutoiveet />
          {hasOphPaaKayttajaRole(user?.authorities) && (
            <OvaraRadioGroup
              label={'Käytä uutta kyselyä'}
              options={RADIOGROUP_BOOLEAN_OPTIONS}
              value={getSelectedRadioGroupValue(uusiTilasto)}
              onChange={(e) => changeRadioGroupSelection(e, setUusiTilasto)}
            />
          )}
          <FormButtons disabled={isDisabled} downloadExcel={handleDownload} />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
