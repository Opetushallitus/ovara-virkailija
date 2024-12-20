import { useFetchHaut } from '@/app/hooks/useFetchHaut';
import {
  MultiComboBox,
  SelectOption,
} from '@/app/components/form/multicombobox';
import { useSearchParams } from '@/app/hooks/useSearchParams';
import { isEmpty } from 'remeda';
import { useTranslations } from 'next-intl';

export const Haku = () => {
  const t = useTranslations();
  // TODO: Lisää lokalisointi
  const locale = 'fi';
  const haut = useFetchHaut() || [];

  const { selectedHaut, setSelectedHaut } = useSearchParams();

  const changeAlkamiskaudet = (
    _: React.SyntheticEvent,
    value: Array<SelectOption>,
  ) => {
    return setSelectedHaut(isEmpty(value) ? null : value?.map((v) => v.value));
  };

  return (
    <MultiComboBox
      id={'haku'}
      label={`${t('raportti.haku')}`}
      value={selectedHaut ?? []}
      options={haut?.map((haku) => {
        return {
          value: haku.haku_oid,
          label: haku.haku_nimi[locale] || '',
        };
      })}
      onChange={changeAlkamiskaudet}
      required={true}
    />
  );
};
