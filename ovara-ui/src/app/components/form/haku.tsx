import { MultiComboBox } from '@/app/components/form/multicombobox';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { Kielistetty, LanguageCode } from '@/app/lib/types/common';
import { useFetchHaut } from '@/app/hooks/useFetchHaut';
import { useSearchParams } from 'next/navigation';
import { changeMultiComboBoxSelection } from '@/app/components/form/utils';

type Haku = {
  haku_oid: string;
  haku_nimi: Kielistetty;
};

export const Haku = () => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli ?? 'fi') as LanguageCode;

  const { selectedHaut, setSelectedHaut } = useCommonSearchParams();

  const queryParams = useSearchParams();
  const queryParamsStr = queryParams.toString();
  const queryParamsWithHauntyyppi = new URLSearchParams(queryParamsStr);
  queryParamsWithHauntyyppi.set('haun_tyyppi', 'toinen_aste');
  const { data } = useFetchHaut(queryParamsWithHauntyyppi.toString(), fetchEnabled,);

  const haut: Haku[] = data || [];

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
      onChange={(e, value) =>
        changeMultiComboBoxSelection(e, value, setSelectedHaut)
      }
      required={true}
    />
  );
};
