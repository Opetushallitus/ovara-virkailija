'use client';
import { OphTypography } from '@opetushallitus/oph-design-system';
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
import {
  buildKkHakijatTiedonsiirtoParams,
  hasOvaraKkHakeneetRole,
} from '@/app/lib/utils';
import { useKkHakijatTiedonsiirtoSearchParams } from '@/app/hooks/searchParams/useKkHakijatTiedonsiirtoSearchParams';
import { useDownloadWithErrorBoundary } from '@/app/hooks/useDownloadWithErrorBoundary';
import { downloadExcel } from '@/app/components/form/utils';

export default function KkHakijatTiedonsiirto() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasAccess = hasOvaraKkHakeneetRole(user?.authorities);
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

  // Rajaimet leikataan backendissa keskenään, ja hakukohderyhmä riittää yksinään
  // rajaimeksi -- organisaatio on siis pakollinen vain ilman ryhmävalintaa.
  const isDisabled =
    isNullish(selectedHaku) ||
    isNullish(selectedValintarajaus) ||
    (isNullish(selectedOrganisaatio) && isNullish(selectedHakukohderyhma));

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
        }),
      ),
    );

  return (
    <MainContainer>
      {hasAccess ? (
        <FormBox>
          {isLoading && <SpinnerModal open={isLoading} />}
          <OphTypography>{t('yleinen.pakolliset-kentat')}</OphTypography>
          <KoulutuksenAlkaminen />
          <HakuSingle
            haunTyyppi="korkeakoulu"
            value={selectedHaku}
            onChange={changeHaku}
            required
          />
          <OrganisaatioSingle
            value={selectedOrganisaatio}
            onChange={changeOrganisaatio}
            required={isNullish(selectedHakukohderyhma)}
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
            required
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
