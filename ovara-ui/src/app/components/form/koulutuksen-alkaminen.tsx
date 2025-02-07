import { useMemo } from 'react';
import { useTranslate } from '@tolgee/react';
import { getSortedKoulutuksenAlkamisKaudet } from '@/app/lib/utils';
import {
  MultiComboBox,
  SelectOption,
} from '@/app/components/form/multicombobox';
import { useSearchParams } from '@/app/hooks/searchParams/useSearchParams';
import { isEmpty } from 'remeda';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';

export const KoulutuksenAlkaminen = () => {
  const { t } = useTranslate();

  const { data } = useQuery({
    queryKey: ['fetchAlkamisvuodet'],
    queryFn: () => doApiFetch('alkamisvuodet'),
  });
  const alkamisvuodet = data;
  const sortedAlkamiskaudet = useMemo(
    () => getSortedKoulutuksenAlkamisKaudet(alkamisvuodet),
    [alkamisvuodet],
  );

  const { selectedAlkamiskaudet, setSelectedAlkamiskaudet } = useSearchParams();

  const changeAlkamiskaudet = (
    _: React.SyntheticEvent,
    value: Array<SelectOption>,
  ) => {
    return setSelectedAlkamiskaudet(
      isEmpty(value) ? null : value?.map((v) => v.value),
    );
  };

  return (
    <MultiComboBox
      id={'alkamiskaudet'}
      label={`${t('raportti.alkamiskausi')}`}
      value={selectedAlkamiskaudet ?? []}
      options={sortedAlkamiskaudet?.map((kausi) => {
        const alkamiskaudenNimi = t(kausi.alkamiskausinimi);
        return {
          value: kausi.value,
          label: kausi.alkamisvuosi
            ? `${kausi.alkamisvuosi} ${alkamiskaudenNimi}`
            : `${alkamiskaudenNimi}`,
        };
      })}
      onChange={changeAlkamiskaudet}
      required={true}
    />
  );
};
