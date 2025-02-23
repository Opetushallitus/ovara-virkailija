'use client';
import { OphTypography } from '@opetushallitus/oph-design-system';
import { MainContainer } from '@/app/components/main-container';
import { FormBox } from '@/app/components/form/form-box';
import { FormButtons } from '@/app/components/form/form-buttons';
import { Box, Divider } from '@mui/material';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { hasOvaraKkRole } from '@/app/lib/utils';
import { useSearchParams } from 'next/navigation';
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
import { NaytaYoArvosanat } from '@/app/components/form/nayta';
import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';

export default function Hakijat() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasKkRights = hasOvaraKkRole(user?.authorities);
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';
  const queryParams = useSearchParams();
  const organisaatiot = useFetchOrganisaatiohierarkiat();
  const alkamiskausi = queryParams.get('alkamiskausi');
  const haku = queryParams.get('haku');
  const oppilaitos = queryParams.get('oppilaitos');
  const toimipiste = queryParams.get('toimipiste');

  const { selectedYoArvosanat } = useHakijatSearchParams();

  const queryParamsStr = useQueryParams().toString();
  const queryParamsWithDefaults = new URLSearchParams(queryParamsStr);
  queryParamsWithDefaults.set(
    'nayta-yo-arvosanat',
    selectedYoArvosanat.toString(),
  );

  const isDisabled = !(alkamiskausi && haku && (oppilaitos || toimipiste));
  const [isLoading, setIsLoading] = useState(false);

  return (
    <MainContainer>
      {hasKkRights ? (
        <FormBox>
          {isLoading && <SpinnerModal open={isLoading} />}
          <OphTypography>{t('yleinen.pakolliset-kentat')}</OphTypography>
          <KoulutuksenAlkaminen t={t} />
          <Haku haunTyyppi={'korkeakoulu'} locale={locale} t={t} />
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
          </Box>
          <Divider />
          <Hakukohde locale={locale} t={t} />
          <Valintatieto t={t} />
          <Vastaanottotieto t={t} />
          <Markkinointilupa t={t} />
          <NaytaYoArvosanat t={t} />
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
