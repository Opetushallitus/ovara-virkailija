import {
  MultiComboBox,
  SelectOption,
} from '@/app/components/form/multicombobox';
import { useSearchParams } from '@/app/hooks/searchParams/useSearchParams';
import { isEmpty } from 'remeda';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { Kielistetty, LanguageCode } from '@/app/lib/types/common';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';

type Haku = {
  haku_oid: string;
  haku_nimi: Kielistetty;
};

export const Haku = () => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli ?? 'fi') as LanguageCode;

  const { data } = useQuery({
    queryKey: ['fetchHaut'],
    queryFn: () => doApiFetch('haut'),
  });

  const haut: Haku[] = data || [];

  const { selectedHaut, setSelectedHaut } = useSearchParams();

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
