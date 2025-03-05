import { SelectChangeEvent } from '@mui/material';
import { useTranslate } from '@tolgee/react';
import { LocalizedSelect } from '@/app/components/form/localized-select';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';

const tulostustavat = ({
  t,
  kk,
}: {
  t: (key: string) => string;
  kk?: boolean;
}) => {
  const tavat = kk
    ? [
        'koulutustoimijoittain',
        'oppilaitoksittain',
        'toimipisteittain',
        'okmohjauksenaloittain',
        'hauittain',
        'hakukohteittain',
        'hakukohderyhmittain',
        'kansalaisuuksittain',
      ]
    : [
        'koulutustoimijoittain',
        'oppilaitoksittain',
        'toimipisteittain',
        'koulutusaloittain',
        'hakukohteittain',
      ];
  return tavat.map((tulostustapa) => {
    return {
      value: tulostustapa,
      label: t(`raportti.tulostustapa.${tulostustapa}`),
    };
  });
};

export const Tulostustapa = ({ kk }: { kk?: boolean }) => {
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
      options={tulostustavat({ t, kk })}
      onChange={changeTulostustapa}
      required={true}
    />
  );
};
