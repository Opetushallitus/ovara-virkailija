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
import { OrganisaatioSingle } from '@/app/components/form/organisaatio-single';
import { ValintarajausSelect } from '@/app/components/form/valintarajaus-select';
import { SpinnerModal } from '@/app/components/form/spinner-modal';
import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';
import { hasOvaraKkHakeneetRole } from '@/app/lib/utils';
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
    selectedOrganisaatio,
    setSelectedOrganisaatio,
    selectedValintarajaus,
    setSelectedValintarajaus,
    emptyAllKkHakijatTiedonsiirtoParams,
  } = useKkHakijatTiedonsiirtoSearchParams();

  const hakukohdeSet = !isNullish(selectedHakukohde);
  const organisaatioSet = !isNullish(selectedOrganisaatio);
  const hakukohdeOrOrganisaatioSet = hakukohdeSet !== organisaatioSet;

  const isDisabled =
    isNullish(selectedHaku) ||
    !hakukohdeOrOrganisaatioSet ||
    isNullish(selectedValintarajaus);

  const handleDownload = () =>
    run(() => {
      const params = new URLSearchParams();
      if (selectedHaku) params.set('hakuOid', selectedHaku);
      if (selectedHakukohde) params.set('hakukohdeOid', selectedHakukohde);
      if (selectedOrganisaatio)
        params.set('organisaatioOid', selectedOrganisaatio);
      if (selectedValintarajaus)
        params.set('valintarajaus', selectedValintarajaus);
      return downloadExcel('external/kkhakijat/excel', params.toString());
    });

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
            onChange={setSelectedHaku}
            required
          />
          <HakukohdeSingle
            hakuOid={selectedHaku}
            value={selectedHakukohde}
            onChange={setSelectedHakukohde}
            disabled={organisaatioSet}
          />
          <OrganisaatioSingle
            value={selectedOrganisaatio}
            onChange={setSelectedOrganisaatio}
            disabled={hakukohdeSet}
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
