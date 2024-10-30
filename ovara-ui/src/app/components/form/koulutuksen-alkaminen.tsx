import { useMemo } from 'react';
import { useTranslations } from 'next-intl';
import { useFetchAlkamisvuodet } from '@/app/hooks/useFetchAlkamisvuodet';
import { getSortedKoulutuksenAlkamisKaudet } from '@/app/lib/utils';
import {
  MultiComboBox,
  OphMultiComboBoxOption,
} from '@/app/components/form/multicombobox';
import { useSearchParams } from '@/app/hooks/useSearchParams';
import { isEmpty } from 'remeda';

export const KoulutuksenAlkaminen = ({ t }: { t: typeof useTranslations }) => {
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
    <MultiComboBox
      id={'alkamiskaudet'}
      label={`${t('raportti.alkamiskausi')}`}
      options={sortedAlkamiskaudet?.map((kausi) => {
        const alkamiskaudenNimi = `${t(kausi.alkamiskausinimi)}`;
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
