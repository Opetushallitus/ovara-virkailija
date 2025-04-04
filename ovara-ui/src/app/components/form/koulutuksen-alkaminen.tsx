import { useMemo } from 'react';
import { useTranslate } from '@tolgee/react';
import { getSortedKoulutuksenAlkamisKaudet } from '@/app/lib/utils';
import {
  MultiComboBox,
  SelectOption,
} from '@/app/components/form/multicombobox';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { isEmpty } from 'remeda';

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

  const { selectedAlkamiskaudet, setSelectedAlkamiskaudet, setSelectedHaut } =
    useCommonSearchParams();

  const changeAlkamiskaudet = (
    _: React.SyntheticEvent,
    value: Array<SelectOption>,
  ) => {
    if (isEmpty(value)) {
      setSelectedHaut(null);
    }
    return setSelectedAlkamiskaudet(
      isEmpty(value) ? null : value?.map((v) => v.value),
    );
  };

  return (
    <MultiComboBox
      id={'alkamiskaudet'}
      label={t('raportti.alkamiskausi')}
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
      onChange={(e, value) => changeAlkamiskaudet(e, value)}
      required={true}
      sortOptions={false}
    />
  );
};
