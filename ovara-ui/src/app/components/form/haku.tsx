import { MultiComboBox } from '@/app/components/form/multicombobox';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { Kielistetty, LanguageCode } from '@/app/lib/types/common';
import { useFetchHaut } from '@/app/hooks/useFetchHaut';
import { useSearchParams } from 'next/navigation';
import { changeMultiComboBoxSelection } from '@/app/components/form/utils';
import { isNullish } from 'remeda';

type Haku = {
  haku_oid: string;
  haku_nimi: Kielistetty;
};

export const Haku = ({
  haunTyyppi,
  locale,
  t,
}: {
  haunTyyppi: string;
  locale: LanguageCode;
  t: (key: string) => string;
}) => {
  const { selectedHaut, setSelectedHaut, selectedAlkamiskaudet } =
    useCommonSearchParams();
  const fetchEnabled = !isNullish(selectedAlkamiskaudet);
  const queryParams = useSearchParams();
  const queryParamsStr = queryParams.toString();
  const queryParamsWithHauntyyppi = new URLSearchParams(queryParamsStr);
  queryParamsWithHauntyyppi.set('haun_tyyppi', haunTyyppi);

  const { data } = useFetchHaut(
    queryParamsWithHauntyyppi.toString(),
    fetchEnabled,
  );

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
