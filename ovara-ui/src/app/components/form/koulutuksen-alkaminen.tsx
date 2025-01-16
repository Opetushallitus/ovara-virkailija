import { useMemo } from 'react';
import { useTranslate } from '@tolgee/react';
import { useFetchAlkamisvuodet } from '@/app/hooks/useFetchAlkamisvuodet';
import { getSortedKoulutuksenAlkamisKaudet } from '@/app/lib/utils';
import {
  MultiComboBox,
  SelectOption,
} from '@/app/components/form/multicombobox';
import { useSearchParams } from '@/app/hooks/useSearchParams';
import { isEmpty } from 'remeda';

export const KoulutuksenAlkaminen = () => {
  const { t } = useTranslate();
  const alkamisvuodet = useFetchAlkamisvuodet();
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
