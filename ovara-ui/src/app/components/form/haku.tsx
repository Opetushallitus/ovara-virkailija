import { MultiComboBox } from '@/app/components/form/multicombobox';
import { useSearchParams } from '@/app/hooks/searchParams/useSearchParams';
import { Kielistetty, LanguageCode } from '@/app/lib/types/common';
import { useFetchHaut } from '@/app/hooks/useFetchHaut';
import { useSearchParams as useQueryParams } from 'next/navigation';
import { changeMultiComboBoxSelection } from './utils';

type Haku = {
  haku_oid: string;
  haku_nimi: Kielistetty;
};

export const Haku = ({
  locale,
  t,
}: {
  locale: LanguageCode;
  t: (key: string) => string;
}) => {
  const { selectedHaut, setSelectedHaut } = useSearchParams();

  const queryParams = useQueryParams();
  const queryParamsStr = queryParams.toString();
  const queryParamsWithHauntyyppi = new URLSearchParams(queryParamsStr);
  queryParamsWithHauntyyppi.set('haun_tyyppi', 'toinen_aste');
  const { data } = useFetchHaut(queryParamsWithHauntyyppi.toString());

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
