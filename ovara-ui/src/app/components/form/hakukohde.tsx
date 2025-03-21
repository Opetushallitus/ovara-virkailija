import { MultiComboBox } from '@/app/components/form/multicombobox';
import { useFetchHakukohteet } from '@/app/hooks/useFetchHakukohteet';
import { Kielistetty, LanguageCode } from '@/app/lib/types/common';
import { useSearchParams as useQueryParams } from 'next/navigation';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { changeMultiComboBoxSelection } from './utils';
import { isNullish } from 'remeda';

type Hakukohde = {
  hakukohde_oid: string;
  hakukohde_nimi: Kielistetty;
};

export const Hakukohde = ({
  locale,
  t,
  ...props
}: {
  locale: LanguageCode;
  t: (key: string) => string;
  [key: string]: unknown;
}) => {
  const { selectedHakukohteet, setSelectedHakukohteet } =
    useCommonSearchParams();

  const queryParams = useQueryParams();
  const haku = queryParams.get('haku');
  const fetchEnabled = !isNullish(haku);

  const queryParamsStr = queryParams.toString();
  const { data } = useFetchHakukohteet(queryParamsStr, fetchEnabled);

  const hakukohteet: Array<Hakukohde> = data || [];

  return (
    <MultiComboBox
      id={'hakukohde'}
      label={t('raportti.hakukohde')}
      value={selectedHakukohteet ?? []}
      options={hakukohteet?.map((hakukohde) => {
        return {
          value: hakukohde?.hakukohde_oid,
          label: hakukohde?.hakukohde_nimi[locale]
            ? `${hakukohde.hakukohde_nimi[locale]} (${hakukohde.hakukohde_oid})`
            : '',
        };
      })}
      onChange={(e, value) =>
        changeMultiComboBoxSelection(e, value, setSelectedHakukohteet)
      }
      {...props}
    />
  );
};
