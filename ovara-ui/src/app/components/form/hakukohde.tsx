import {
  MultiComboBox,
  SelectOption,
} from '@/app/components/form/multicombobox';
import { isEmpty } from 'remeda';
import { useFetchHakukohteet } from '@/app/hooks/useFetchHakukohteet';
import { Kielistetty, LanguageCode } from '@/app/lib/types/common';
import { useSearchParams as useQueryParams } from 'next/navigation';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';

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
  const queryParamsStr = queryParams.toString();
  const { data } = useFetchHakukohteet(queryParamsStr);

  const hakukohteet: Array<Hakukohde> = data || [];

  const changeHakukohteet = (
    _: React.SyntheticEvent,
    value: Array<SelectOption>,
  ) => {
    return setSelectedHakukohteet(
      isEmpty(value) ? null : value?.map((v) => v.value),
    );
  };

  return (
    <MultiComboBox
      id={'hakukohde'}
      label={t('raportti.hakukohde')}
      value={selectedHakukohteet ?? []}
      options={hakukohteet?.map((hakukohde) => {
        return {
          value: hakukohde?.hakukohde_oid,
          label:
            `${hakukohde?.hakukohde_nimi[locale]} (${hakukohde.hakukohde_oid})` ||
            '',
        };
      })}
      onChange={changeHakukohteet}
      {...props}
    />
  );
};
