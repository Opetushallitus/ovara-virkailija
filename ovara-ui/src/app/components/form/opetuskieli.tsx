import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { Koodi } from '@/app/lib/types/common';
import { MultiComboBox } from '@/app/components/form/multicombobox';
import { useAsiointiKieli } from '@/app/hooks/useAsiointikieli';
import { useTranslate } from '@tolgee/react';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';
import {
  changeMultiComboBoxSelection,
  getKoodiOptions,
} from '@/app/components/form/utils';

const getSortedOpetuskielet = (opetuskielet: Array<Koodi>): Array<Koodi> => {
  return opetuskielet.sort((a, b) => a.koodiarvo.localeCompare(b.koodiarvo));
};

export const Opetuskieli = () => {
  const locale = useAsiointiKieli();
  const { t } = useTranslate();
  const { selectedOpetuskielet, setSelectedOpetuskielet } =
    useHakeneetSearchParams();

  const { data } = useQuery({
    queryKey: ['fetchOpetuskielet'],
    queryFn: () => doApiFetch('opetuskielet'),
  });

  const opetuskielet: Array<Koodi> = data || [];
  const sortedOpetuskielet = getSortedOpetuskielet(opetuskielet);

  return (
    <MultiComboBox
      id={'opetuskielet'}
      label={`${t('raportti.opetuskieli')}`}
      value={selectedOpetuskielet ?? []}
      options={getKoodiOptions(locale, sortedOpetuskielet)}
      onChange={(e, value) =>
        changeMultiComboBoxSelection(e, value, setSelectedOpetuskielet)
      }
      sortOptions={false}
      required={false}
    />
  );
};
