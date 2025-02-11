import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { Kielistetty } from '@/app/lib/types/common';
import {
  MultiComboBox,
  SelectOption,
} from '@/app/components/form/multicombobox';
import { isEmpty } from 'remeda';
import { useAsiointiKieli } from '@/app/hooks/useAsiointikieli';
import { useTranslate } from '@tolgee/react';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';

type Opetuskieli = {
  koodiarvo: string;
  nimi: Kielistetty;
};

const getSortedOpetuskielet = (
  opetuskielet: Array<Opetuskieli>,
): Array<Opetuskieli> => {
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

  const opetuskielet: Array<Opetuskieli> = data || [];
  const sortedOpetuskielet = getSortedOpetuskielet(opetuskielet);

  const changeOpetuskielet = (
    _: React.SyntheticEvent,
    value: Array<SelectOption>,
  ) => {
    return setSelectedOpetuskielet(
      isEmpty(value) ? null : value?.map((v) => v.value),
    );
  };

  return (
    <MultiComboBox
      id={'opetuskielet'}
      label={`${t('raportti.opetuskieli')}`}
      value={selectedOpetuskielet ?? []}
      options={sortedOpetuskielet?.map((opetuskieli) => {
        return {
          value: opetuskieli?.koodiarvo,
          label: opetuskieli?.nimi[locale],
        };
      })}
      onChange={changeOpetuskielet}
      required={false}
    />
  );
};
