'use client';
import { OphTypography } from '@opetushallitus/oph-design-system';
import { useTranslate } from '@tolgee/react';
import { KoulutuksenAlkaminen } from '@/app/components/form/koulutuksen-alkaminen';
import { Haku } from '@/app/components/form/haku';
import {
  HakukohteenTila,
  KoulutuksenTila,
  ToteutuksenTila,
} from '@/app/components/form/tila';
import { FormBox } from '@/app/components/form/form-box';
import { FormButtons } from '@/app/components/form/form-buttons';
import { Divider } from '@mui/material';
import { useSearchParams } from 'next/navigation';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { hasOvaraKkRole } from '@/app/lib/utils';
import { SpinnerModal } from '@/app/components/form/spinner-modal';
import { downloadExcel } from '@/app/components/form/utils';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { MainContainer } from '@/app/components/main-container';
import {
  Oppilaitos,
  Toimipiste,
} from '@/app/components/form/organisaatiovalikot';
import { useFetchOrganisaatiohierarkiat } from '@/app/hooks/useFetchOrganisaatiohierarkiat';
import { Hakukohderyhma } from '@/app/components/form/hakukohderyhma';
import { KkTutkinnonTaso } from '@/app/components/form/kk-tutkinnon-taso';
import { isEmpty } from 'remeda';
import { Tulostustapa } from '@/app/components/form/tulostustapa';
import { useDownloadWithErrorBoundary } from '@/app/hooks/useDownloadWithErrorBoundary';

export default function KoulutuksetToteutuksetHakukohteet() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasKkRights = hasOvaraKkRole(user?.authorities);
  const {
    selectedAlkamiskaudet,
    selectedHaut,
    selectedOppilaitokset,
    selectedToimipisteet,
    selectedHakukohderyhmat,
  } = useCommonSearchParams();
  const { data: organisaatiot } = useFetchOrganisaatiohierarkiat();

  const isDisabled =
    [selectedAlkamiskaudet || [], selectedHaut || []].some(isEmpty) ||
    [
      selectedOppilaitokset || [],
      selectedToimipisteet || [],
      selectedHakukohderyhmat || [],
    ].every(isEmpty);
  const { run, isLoading } = useDownloadWithErrorBoundary();

  const queryParamsStr = useSearchParams().toString();

  const KOULUTUKSITTAIN = 'koulutuksittain';
  const tulostustavat = [KOULUTUKSITTAIN, 'toteutuksittain', 'hakukohteittain'];
  const handleDownload = () =>
    run(() =>
      downloadExcel('kk-koulutukset-toteutukset-hakukohteet', queryParamsStr),
    );

  return (
    <MainContainer>
      {hasKkRights ? (
        <FormBox>
          {isLoading && <SpinnerModal open />}
          <OphTypography>{t('yleinen.pakolliset-kentat')}</OphTypography>
          <KoulutuksenAlkaminen />
          <Haku haunTyyppi="korkeakoulu" />
          <Divider />
          <OphTypography>
            {t('raportti.vahintaan-yksi-vaihtoehdoista')}
          </OphTypography>
          <Oppilaitos organisaatiot={organisaatiot} />
          <Toimipiste organisaatiot={organisaatiot} />
          <Hakukohderyhma />
          <Divider />
          <Tulostustapa
            tulostustavat={tulostustavat}
            defaultValue={KOULUTUKSITTAIN}
          />
          <KoulutuksenTila />
          <ToteutuksenTila />
          <HakukohteenTila />
          <KkTutkinnonTaso />
          <FormButtons disabled={isDisabled} downloadExcel={handleDownload} />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
