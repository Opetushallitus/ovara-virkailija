'use client';
import { useMemo } from 'react';
import { Typography } from '@mui/material';
import { MainContainer } from '@/app/components/main-container';
import { useTranslations } from 'next-intl';

import { useFetchAlkamisvuodet } from '@/app/hooks/useFetchAlkamisvuodet';
import { getSortedKoulutuksenAlkamisKaudet } from '@/app/lib/utils';
import { OvaraFormControl } from '@/app/components/form/oph-form-control';
import { MultiComboBox } from '@/app/components/multicombobox';
import { useSearchParams } from '@/app/hooks/useSearchParams';
import { isEmpty } from 'remeda';
import { OphMultiComboBoxOption } from '@/app/components/multiselect';

export default function KoulutuksetToteutuksetHakukohteet() {
  const t = useTranslations();
  const alkamisvuodet = useFetchAlkamisvuodet();
  const sortedAlkamiskaudet = useMemo(
    () => getSortedKoulutuksenAlkamisKaudet(alkamisvuodet),
    [alkamisvuodet],
  );

  const { setSelectedAlkamiskaudet } = useSearchParams();

  const changeAlkamiskaudet = (
    _: React.SyntheticEvent,
    value: Array<OphMultiComboBoxOption>,
  ) => {
    return setSelectedAlkamiskaudet(
      isEmpty(value) ? null : value?.map((v) => v.value),
    );
  };

  return (
    <MainContainer>
      <Typography>{t('yleinen.pakolliset-kentat')}</Typography>
      <OvaraFormControl
        label={`${t('raportti.alkamiskausi')} *`}
        sx={{ textAlign: 'left', flex: '1 0 180px' }}
        renderInput={() => (
          <MultiComboBox
            id={'alkamiskaudet'}
            options={sortedAlkamiskaudet?.map((kausi) => {
              return {
                value: kausi.value,
                label: `${kausi.alkamisvuosi} ${t(kausi.alkamiskausinimi)}`,
              };
            })}
            onChange={changeAlkamiskaudet}
          />
        )}
      />
    </MainContainer>
  );
}
