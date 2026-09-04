'use client';
import { OphTypography } from '@opetushallitus/oph-design-system';
import { Divider } from '@mui/material';
import { useTranslate } from '@tolgee/react';
import { isNullish } from 'remeda';

import { MainContainer } from '@/app/components/main-container';
import { FormBox } from '@/app/components/form/form-box';
import { FormButtons } from '@/app/components/form/form-buttons';
import { KoulutuksenAlkaminen } from '@/app/components/form/koulutuksen-alkaminen';
import { HakuSingle } from '@/app/components/form/haku-single';
import { HakukohdeSingle } from '@/app/components/form/hakukohde-single';
import { HakukohderyhmaSingle } from '@/app/components/form/hakukohderyhma-single';
import { OrganisaatioSingle } from '@/app/components/form/organisaatio-single';
import { ValintarajausSelect } from '@/app/components/form/valintarajaus-select';
import { SpinnerModal } from '@/app/components/form/spinner-modal';
import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';
import { OvaraTextInput } from '@/app/components/form/OvaraTextInput';
import {
  buildKkHakijatTiedonsiirtoParams,
  hasKkHakijatKaikkiTiedotRight,
  hasOvaraKkHakeneetRole,
} from '@/app/lib/utils';
import { useKkHakijatTiedonsiirtoSearchParams } from '@/app/hooks/searchParams/useKkHakijatTiedonsiirtoSearchParams';
import { useDownloadWithErrorBoundary } from '@/app/hooks/useDownloadWithErrorBoundary';
import { downloadExcel } from '@/app/components/form/utils';

export default function KkHakijatTiedonsiirto() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasAccess = hasOvaraKkHakeneetRole(user?.authorities);
  // Oppijanumerohaku on rekisterinpitäjän toiminto: backend palauttaa muille 403, joten
  // kenttää ei näytetä lainkaan ilman oikeutta.
  const hasKaikkiTiedot = hasKkHakijatKaikkiTiedotRight(user?.authorities);
  const { run, isLoading } = useDownloadWithErrorBoundary();

  const {
    selectedHaku,
    setSelectedHaku,
    selectedHakukohde,
    setSelectedHakukohde,
    selectedHakukohderyhma,
    setSelectedHakukohderyhma,
    selectedOrganisaatio,
    setSelectedOrganisaatio,
    selectedValintarajaus,
    setSelectedValintarajaus,
    selectedOppijanumero,
    setSelectedOppijanumero,
    emptyAllKkHakijatTiedonsiirtoParams,
  } = useKkHakijatTiedonsiirtoSearchParams();

  // Hakukohdevalikko on rajattu haun, organisaation ja hakukohderyhmän mukaan, joten
  // vanha valinta ei ole enää validi kun mikään niistä vaihtuu. Hakukohderyhmävalikko
  // on rajattu vain haun mukaan.
  const changeHaku = (value: string | null) => {
    setSelectedHaku(value);
    setSelectedHakukohderyhma(null);
    setSelectedHakukohde(null);
  };

  const changeOrganisaatio = (value: string | null) => {
    setSelectedOrganisaatio(value);
    setSelectedHakukohde(null);
  };

  const changeHakukohderyhma = (value: string | null) => {
    setSelectedHakukohderyhma(value);
    setSelectedHakukohde(null);
  };

  // Oppijanumero riittää yksinään rajaimeksi, jolloin haku, organisaatio ja valintarajaus
  // eivät ole pakollisia. Annettuina ne rajaavat tulosta edelleen.
  const oppijanumeroGiven =
    !isNullish(selectedOppijanumero) && selectedOppijanumero.trim() !== '';

  // Rajaimet leikataan backendissa keskenään, ja hakukohderyhmä riittää yksinään
  // rajaimeksi -- organisaatio on siis pakollinen vain ilman ryhmävalintaa.
  const isDisabled =
    !oppijanumeroGiven &&
    (isNullish(selectedHaku) ||
      isNullish(selectedValintarajaus) ||
      (isNullish(selectedOrganisaatio) && isNullish(selectedHakukohderyhma)));

  const handleDownload = () =>
    run(() =>
      downloadExcel(
        'external/kkhakijat/excel',
        buildKkHakijatTiedonsiirtoParams({
          hakuOid: selectedHaku,
          hakukohdeOid: selectedHakukohde,
          hakukohderyhmaOid: selectedHakukohderyhma,
          organisaatioOid: selectedOrganisaatio,
          valintarajaus: selectedValintarajaus,
          oppijanumero: selectedOppijanumero,
        }),
      ),
    );

  return (
    <MainContainer>
      {hasAccess ? (
        <FormBox>
          {isLoading && <SpinnerModal open={isLoading} />}
          <OphTypography>{t('yleinen.pakolliset-kentat')}</OphTypography>
          {hasKaikkiTiedot && (
            <>
              <OvaraTextInput
                label={t('raportti.oppijanumero')}
                value={selectedOppijanumero}
                onValueChange={setSelectedOppijanumero}
              />
              <Divider />
            </>
          )}
          <KoulutuksenAlkaminen />
          <HakuSingle
            haunTyyppi="korkeakoulu"
            value={selectedHaku}
            onChange={changeHaku}
            required={!oppijanumeroGiven}
          />
          <OrganisaatioSingle
            value={selectedOrganisaatio}
            onChange={changeOrganisaatio}
            required={!oppijanumeroGiven && isNullish(selectedHakukohderyhma)}
          />
          <HakukohderyhmaSingle
            hakuOid={selectedHaku}
            value={selectedHakukohderyhma}
            onChange={changeHakukohderyhma}
          />
          <HakukohdeSingle
            hakuOid={selectedHaku}
            organisaatioOid={selectedOrganisaatio}
            hakukohderyhmaOid={selectedHakukohderyhma}
            value={selectedHakukohde}
            onChange={setSelectedHakukohde}
          />
          <ValintarajausSelect
            value={selectedValintarajaus}
            onChange={setSelectedValintarajaus}
            required={!oppijanumeroGiven}
          />
          <FormButtons
            disabled={isDisabled}
            downloadExcel={handleDownload}
            fieldsToClear={[emptyAllKkHakijatTiedonsiirtoParams]}
          />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
