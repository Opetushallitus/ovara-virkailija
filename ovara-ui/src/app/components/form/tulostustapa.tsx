import { SelectChangeEvent } from '@mui/material';
import { useTranslate } from '@tolgee/react';
import { LocalizedSelect } from '@/app/components/form/localized-select';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';

const tulostustavat = (t: (key: string) => string) => {
  const tavat = [
    'koulutustoimijoittain',
    'oppilaitoksittain',
    'toimipisteittäin',
    'koulutusaloittain',
    'hakukohteittain',
  ];
  return tavat.map((tulostustapa) => {
    return {
      value: tulostustapa,
      label: t(`yleinen.tulostustapa.${tulostustapa}`),
    };
  });
};

export const Tulostustapa = () => {
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
      value={selectedTulostustapa ?? ''}
      options={tulostustavat(t)}
      onChange={changeTulostustapa}
      required={true}
    />
  );
};
