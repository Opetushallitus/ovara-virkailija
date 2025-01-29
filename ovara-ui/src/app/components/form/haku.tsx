import {
  MultiComboBox,
  SelectOption,
} from '@/app/components/form/multicombobox';
import { useSearchParams } from '@/app/hooks/searchParams/useSearchParams';
import { isEmpty } from 'remeda';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { Kielistetty, LanguageCode } from '@/app/lib/types/common';
import { useFetchHaut } from '@/app/hooks/useFetchHaut';
import { useSearchParams as useQueryParams } from 'next/navigation';

type Haku = {
  haku_oid: string;
  haku_nimi: Kielistetty;
};

export const Haku = () => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli ?? 'fi') as LanguageCode;

  const { selectedHaut, setSelectedHaut } = useSearchParams();

  const queryParams = useQueryParams();
  const alkamiskausiQueryParams = queryParams.get('alkamiskausi');
  const { data } = useFetchHaut(alkamiskausiQueryParams);

  const haut: Haku[] = data || [];

  const changeHaut = (_: React.SyntheticEvent, value: Array<SelectOption>) => {
    return setSelectedHaut(isEmpty(value) ? null : value?.map((v) => v.value));
  };

  return (
    <MultiComboBox
      id={'haku'}
      label={t('raportti.haku')}
      value={selectedHaut ?? []}
      options={haut?.map((haku) => {
        return {
          value: haku.haku_oid,
          label: haku.haku_nimi[locale] || '',
        };
      })}
      onChange={changeHaut}
      required={true}
    />
  );
};
