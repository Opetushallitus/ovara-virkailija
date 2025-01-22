import {
  MultiComboBox,
  SelectOption,
} from '@/app/components/form/multicombobox';
import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { isEmpty } from 'remeda';
import { useFetchHakukohteet } from '@/app/hooks/useFetchHakukohteet';
import { LanguageCode } from '@/app/lib/types/common';

export const Hakukohde = ({
  locale,
  t,
}: {
  locale: LanguageCode;
  t: (key: string) => string;
}) => {
  const { selectedHakukohteet, setSelectedHakukohteet } =
    useHakijatSearchParams();

  const hakukohteet = useFetchHakukohteet() || [];

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
      sx={{ paddingTop: 0 }}
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
    />
  );
};
