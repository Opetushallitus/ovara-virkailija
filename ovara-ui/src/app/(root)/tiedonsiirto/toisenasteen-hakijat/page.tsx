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
    selectedOppilaitos,
    setSelectedOppilaitos,
    selectedToimipiste,
    setSelectedToimipiste,
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

  // Oppilaitos rajaa toimipistevalikon, joten oppilaitoksen vaihtuessa aiempi
  // toimipiste ei ole enää validi. Toimipisteen valinta sen sijaan jättää
  // oppilaitoksen ennalleen -- kyse on tarkennuksesta, ei vaihtoehdosta.
  // Tyhjennys on tehtävä setterin kautta, joka kirjoittaa sekä URL-parametrin
  // että localStoragen.
  const changeOppilaitos = (value: string | null) => {
    setSelectedOppilaitos(value);
    setSelectedToimipiste(null);
    setSelectedHakukohde(null);
  };

  const changeToimipiste = (value: string | null) => {
    setSelectedToimipiste(value);
    setSelectedHakukohde(null);
  };

  // Tarkin valinta voittaa: toimipiste rajaa oppilaitosta tarkemmin. Kumpi tahansa
  // yksinään riittää rajaimeksi, joten pelkän toimipisteoikeuden saanut käyttäjä voi
  // ajaa raportin vaikkei näe yhtään oppilaitosta.
  const selectedOrganisaatioOid = selectedToimipiste ?? selectedOppilaitos;
  const orgMissing =
    isNullish(selectedOppilaitos) && isNullish(selectedToimipiste);

  const isDisabled =
    isNullish(selectedHaku) || orgMissing || isNullish(selectedValintarajaus);

  const handleDownload = () =>
    run(() =>
      downloadExcel(
        'external/toisenasteenhakijat/excel',
        buildTiedonsiirtoParams({
          hakuOid: selectedHaku,
          hakukohdeOid: selectedHakukohde,
          organisaatioOid: selectedOrganisaatioOid,
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
            value={selectedOppilaitos}
            onChange={changeOppilaitos}
            required={orgMissing}
          />
          <OrganisaatioSingle
            taso="toimipiste"
            rajaavaOppilaitos={selectedOppilaitos}
            value={selectedToimipiste}
            onChange={changeToimipiste}
            required={orgMissing}
          />
          <HakukohdeSingle
            hakuOid={selectedHaku}
            organisaatioOid={selectedOrganisaatioOid}
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
