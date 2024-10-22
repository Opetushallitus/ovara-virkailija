'use client';
import { useMemo } from 'react';
import { Typography, SelectChangeEvent } from '@mui/material';
import { MainContainer } from '@/app/components/main-container';
import { useTranslations } from 'next-intl';

import { useFetchAlkamisvuodet } from '@/app/hooks/useFetchAlkamisvuodet';
import { getSortedKoulutuksenAlkamisKaudet } from '@/app/lib/utils';
import { OvaraFormControl } from '@/app/components/form/oph-form-control';
import { MultiSelect } from '@/app/components/multiselect';
import { useSearchParams } from '@/app/hooks/useSearchParams';

export default function KoulutuksetToteutuksetHakukohteet() {
  const t = useTranslations();
  const alkamisvuodet = useFetchAlkamisvuodet();
  const sortedAlkamiskaudet = useMemo(
    () => getSortedKoulutuksenAlkamisKaudet(alkamisvuodet),
    [alkamisvuodet],
  );

  const { selectedAlkamiskaudet, setSelectedAlkamiskaudet } = useSearchParams();

  const changeAlkamiskaudet = (e: SelectChangeEvent) => {
    setSelectedAlkamiskaudet(e.target.value);
  };

  return (
    <MainContainer>
      <Typography>{t('yleinen.pakolliset-kentat')}</Typography>
      <OvaraFormControl
        label={`${t('raportti.alkamiskausi')} *`}
        sx={{ textAlign: 'left', flex: '1 0 180px' }}
        renderInput={({ labelId }) => (
          <MultiSelect
            sx={{ width: '100%' }}
            labelId={labelId}
            value={selectedAlkamiskaudet ?? []}
            onChange={changeAlkamiskaudet}
            options={sortedAlkamiskaudet?.map((kausi) => {
              return {
                value: kausi.value,
                label: `${kausi.alkamisvuosi} ${t(kausi.alkamiskausinimi)}`,
              };
            })}
            clearable
          />
        )}
      />
    </MainContainer>
  );
}
