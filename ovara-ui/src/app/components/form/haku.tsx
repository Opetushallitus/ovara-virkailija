import { type useTranslations } from 'next-intl';
import { useFetchHaut } from '@/app/hooks/useFetchHaut';
import {
  MultiComboBox,
  OphMultiComboBoxOption,
} from '@/app/components/form/multicombobox';
import { useSearchParams } from '@/app/hooks/useSearchParams';
import { isEmpty } from 'remeda';

export const Haku = ({ t }: { t: typeof useTranslations }) => {
  // TODO: Lisätään lokalisointi
  const locale = 'fi';
  const haut = useFetchHaut() || [];

  const { setSelectedHaut } = useSearchParams();

  const changeAlkamiskaudet = (
    _: React.SyntheticEvent,
    value: Array<OphMultiComboBoxOption>,
  ) => {
    return setSelectedHaut(isEmpty(value) ? null : value?.map((v) => v.value));
  };
  return (
    <MultiComboBox
      id={'haku'}
      label={`${t('raportti.haku')}`}
      options={haut?.map((haku) => {
        return {
          value: haku.haku_oid,
          label: haku.haku_nimi[locale],
        };
      })}
      onChange={changeAlkamiskaudet}
      required={true}
    />
  );
};
