import { useMemo } from 'react';
import { useTranslate } from '@tolgee/react';
import { getSortedKoulutuksenAlkamisKaudet } from '@/app/lib/utils';
import { MultiComboBox } from '@/app/components/form/multicombobox';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { changeMultiComboBoxSelection } from './utils';

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

  const { selectedAlkamiskaudet, setSelectedAlkamiskaudet } =
    useCommonSearchParams();

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
      onChange={(e, value) =>
        changeMultiComboBoxSelection(e, value, setSelectedAlkamiskaudet)
      }
      required={true}
    />
  );
};
