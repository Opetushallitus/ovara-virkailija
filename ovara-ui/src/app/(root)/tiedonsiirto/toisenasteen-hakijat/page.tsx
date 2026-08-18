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
import { buildTiedonsiirtoParams, hasOvaraHakeneetRole } from '@/app/lib/utils';
import { useToisenasteenHakijatSearchParams } from '@/app/hooks/searchParams/useToisenasteenHakijatSearchParams';
import { useDownloadWithErrorBoundary } from '@/app/hooks/useDownloadWithErrorBoundary';
import { downloadExcel } from '@/app/components/form/utils';

export default function ToisenasteenHakijatTiedonsiirto() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasAccess = hasOvaraHakeneetRole(user?.authorities);
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
    emptyAllToisenasteenHakijatParams,
  } = useToisenasteenHakijatSearchParams();

  // Hakukohdevalikko on rajattu haun ja organisaation mukaan, joten vanha
  // valinta ei ole enää validi kun kumpikaan niistä vaihtuu.
  const changeHaku = (value: string | null) => {
    setSelectedHaku(value);
    setSelectedHakukohde(null);
  };

  const changeOrganisaatio = (value: string | null) => {
    setSelectedOrganisaatio(value);
    setSelectedHakukohde(null);
  };

  const isDisabled =
    isNullish(selectedHaku) ||
    isNullish(selectedOrganisaatio) ||
    isNullish(selectedValintarajaus);

  const handleDownload = () =>
    run(() =>
      downloadExcel(
        'external/toisenasteenhakijat/excel',
        buildTiedonsiirtoParams({
          hakuOid: selectedHaku,
          hakukohdeOid: selectedHakukohde,
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
            haunTyyppi="toinen_aste"
            value={selectedHaku}
            onChange={changeHaku}
            required
          />
          <OrganisaatioSingle
            value={selectedOrganisaatio}
            onChange={changeOrganisaatio}
            required
          />
          <HakukohdeSingle
            hakuOid={selectedHaku}
            organisaatioOid={selectedOrganisaatio}
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
            fieldsToClear={[emptyAllToisenasteenHakijatParams]}
          />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
