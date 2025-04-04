import { SelectChangeEvent } from '@mui/material';
import { useTranslate } from '@tolgee/react';
import { LocalizedSelect } from '@/app/components/form/localized-select';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';

const tulostustavatTranslations = ({
  tulostustavat,
  t,
}: {
  tulostustavat: Array<string>;
  t: (key: string) => string;
}) => {
  return tulostustavat.map((tulostustapa) => {
    return {
      value: tulostustapa,
      label: t(`raportti.tulostustapa.${tulostustapa}`),
    };
  });
};

export const Tulostustapa = ({
  tulostustavat,
  defaultValue = '',
}: {
  tulostustavat: Array<string>;
  defaultValue?: string;
}) => {
  const { t } = useTranslate();
  const { selectedTulostustapa, setSelectedTulostustapa } =
    useHakeneetSearchParams();

  const id = 'tulostustapa';

  const changeTulostustapa = (e: SelectChangeEvent) => {
    setSelectedTulostustapa(e.target.value);
  };

  return (
    <LocalizedSelect
      id={id}
      label={t(`raportti.${id}`)}
      value={selectedTulostustapa ?? defaultValue}
      options={tulostustavatTranslations({ tulostustavat, t })}
      onChange={changeTulostustapa}
      required={true}
    />
  );
};
