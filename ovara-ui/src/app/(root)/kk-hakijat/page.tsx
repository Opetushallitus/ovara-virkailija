'use client';
import { OphTypography } from '@opetushallitus/oph-design-system';
import { FormBox } from '@/app/components/form/form-box';
import { FormButtons } from '@/app/components/form/form-buttons';
import { Box, Divider } from '@mui/material';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { hasOvaraKkRole, isNullishOrEmpty } from '@/app/lib/utils';
import { useFetchOrganisaatiohierarkiat } from '@/app/hooks/useFetchOrganisaatiohierarkiat';

import { KoulutuksenAlkaminen } from '@/app/components/form/koulutuksen-alkaminen';
import { Haku } from '@/app/components/form/haku';
import {
  Oppilaitos,
  Toimipiste,
} from '@/app/components/form/organisaatiovalikot';
import { Hakukohde } from '@/app/components/form/hakukohde';
import { Vastaanottotieto } from '@/app/components/form/vastaanottotieto';
import { Markkinointilupa } from '@/app/components/form/markkinointilupa';
import { useSearchParams as useQueryParams } from 'next/navigation';
import { SpinnerModal } from '@/app/components/form/spinner-modal';
import { downloadExcel } from '@/app/components/form/utils';
import { Valintatieto } from '@/app/components/form/valintatieto';
import {
  NaytaYoArvosanat,
  NaytaHetu,
  NaytaPostiosoite,
} from '@/app/components/form/nayta';
import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { Kansalaisuus } from '@/app/components/form/kansalaisuus';
import { Hakukohderyhma } from '@/app/components/form/hakukohderyhma';
import { MainContainer } from '@/app/components/main-container';
import { useDownloadWithErrorBoundary } from '@/app/hooks/useDownloadWithErrorBoundary';

export default function KkHakijat() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasKkRights = hasOvaraKkRole(user?.authorities);
  const { data: organisaatiot } = useFetchOrganisaatiohierarkiat();

  const {
    selectedHaut,
    selectedOppilaitokset,
    selectedToimipisteet,
    selectedHakukohderyhmat,
  } = useCommonSearchParams();

  const {
    selectedNaytaYoArvosanat,
    selectedNaytaHetu,
    selectedNaytaPostiosoite,
  } = useHakijatSearchParams();

  const queryParamsStr = useQueryParams().toString();
  const queryParamsWithDefaults = new URLSearchParams(queryParamsStr);
  queryParamsWithDefaults.set(
    'nayta-yo-arvosanat',
    selectedNaytaYoArvosanat.toString(),
  );
  queryParamsWithDefaults.set('nayta-hetu', selectedNaytaHetu.toString());
  queryParamsWithDefaults.set(
    'nayta-postiosoite',
    selectedNaytaPostiosoite.toString(),
  );

  const { run, isLoading } = useDownloadWithErrorBoundary();
  const handleDownload = () =>
    run(() => downloadExcel('kk-hakijat', queryParamsWithDefaults.toString()));
  const fetchEnabled =
    !isNullishOrEmpty(selectedHaut) &&
    ![
      selectedOppilaitokset,
      selectedToimipisteet,
      selectedHakukohderyhmat,
    ].every(isNullishOrEmpty);

  return (
    <MainContainer>
      {hasKkRights ? (
        <FormBox>
          {isLoading && <SpinnerModal open={isLoading} />}
          <OphTypography>{t('yleinen.pakolliset-kentat')}</OphTypography>
          <KoulutuksenAlkaminen />
          <Haku haunTyyppi={'korkeakoulu'} />
          <Divider />
          <OphTypography>
            {t('raportti.vahintaan-yksi-vaihtoehdoista')}
          </OphTypography>
          <Box>
            <Oppilaitos organisaatiot={organisaatiot} />
            <Toimipiste organisaatiot={organisaatiot} />
            <Hakukohderyhma />
          </Box>
          <Divider />
          <Hakukohde fetchEnabled={fetchEnabled} sx={{ paddingTop: 0 }} />
          <Valintatieto />
          <Vastaanottotieto />
          <Kansalaisuus />
          <Markkinointilupa />
          <NaytaYoArvosanat />
          <NaytaHetu />
          <NaytaPostiosoite />
          <FormButtons
            disabled={!fetchEnabled}
            downloadExcel={handleDownload}
          />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
