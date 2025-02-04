import { SelectChangeEvent } from '@mui/material';
import { useTranslate } from '@tolgee/react';
import { LocalizedSelect } from '@/app/components/form/localized-select';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';

// TODO kannasta?
const opetuskielet = (t: (key: string) => string) => {
  const kielet = [
    'suomi',
    'ruotsi',
    'englanti',
    'saame',
    'muu',
    'saame',
    'suomiruotsi',
  ];
  return kielet.map((kieli) => {
    return {
      value: kieli,
      label: t(`yleinen.opetuskieli.${kieli}`),
    };
  });
};
// TODO multicombobox, lista koodistosta
export const Opetuskieli = () => {
  const { t } = useTranslate();
  const { selectedOpetuskieli, setSelectedOpetuskieli } =
    useHakeneetSearchParams();

  const id = 'opetuskieli';

  const changeOpetuskieli = (e: SelectChangeEvent) => {
    setSelectedOpetuskieli(e.target.value);
  };

  return (
    <LocalizedSelect
      id={id}
      label={t(`raportti.${id}`)}
      value={selectedOpetuskieli ?? ''}
      options={opetuskielet(t)}
      onChange={changeOpetuskieli}
    />
  );
};
