'use client';
import { OphTypography } from '@opetushallitus/oph-design-system';
import { FormBox } from '@/app/components/form/form-box';
import { FormButtons } from '@/app/components/form/form-buttons';
import { Box, Divider } from '@mui/material';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { hasOvaraKkRole } from '@/app/lib/utils';
import { LanguageCode } from '@/app/lib/types/common';
import { useFetchOrganisaatiohierarkiat } from '@/app/hooks/useFetchOrganisaatiohierarkiat';

import { KoulutuksenAlkaminen } from '@/app/components/form/koulutuksen-alkaminen';
import { Haku } from '@/app/components/form/haku';
import {
  OppilaitosValikko,
  ToimipisteValikko,
} from '@/app/components/form/organisaatiovalikot';
import { Hakukohde } from '@/app/components/form/hakukohde';
import { Vastaanottotieto } from '@/app/components/form/vastaanottotieto';
import { Markkinointilupa } from '@/app/components/form/markkinointilupa';
import { useSearchParams as useQueryParams } from 'next/navigation';
import { useState } from 'react';
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
import { isEmpty } from 'remeda';
import { Kansalaisuus } from '@/app/components/form/kansalaisuus';
import { Hakukohderyhma } from '@/app/components/form/hakukohderyhma';
import { MainContainer } from '@/app/components/main-container';

export default function KkHakijat() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasKkRights = hasOvaraKkRole(user?.authorities);
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';
  const { data: organisaatiot } = useFetchOrganisaatiohierarkiat();

  const {
    selectedAlkamiskaudet,
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

  const isDisabled =
    [selectedAlkamiskaudet || [], selectedHaut || []].some(isEmpty) ||
    [
      selectedOppilaitokset || [],
      selectedToimipisteet || [],
      selectedHakukohderyhmat || [],
    ].every(isEmpty);

  const [isLoading, setIsLoading] = useState(false);

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
            <OppilaitosValikko
              locale={locale}
              organisaatiot={organisaatiot}
              t={t}
            />
            <ToimipisteValikko
              locale={locale}
              organisaatiot={organisaatiot}
              t={t}
            />
            <Hakukohderyhma />
          </Box>
          <Divider />
          <Hakukohde locale={locale} t={t} sx={{ paddingTop: 0 }} />
          <Valintatieto t={t} />
          <Vastaanottotieto t={t} />
          <Kansalaisuus />
          <Markkinointilupa t={t} />
          <NaytaYoArvosanat t={t} />
          <NaytaHetu t={t} />
          <NaytaPostiosoite t={t} />
          <FormButtons
            disabled={isDisabled}
            downloadExcel={() =>
              downloadExcel(
                'kk-hakijat',
                queryParamsWithDefaults.toString(),
                setIsLoading,
              )
            }
          />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
