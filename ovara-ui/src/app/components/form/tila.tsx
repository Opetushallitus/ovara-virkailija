import { useSearchParams } from '@/app/hooks/useSearchParams';
import { LocalizedSelect } from './localized-select';
import { TILAT } from '@/app/lib/constants';
import { SelectChangeEvent } from '@mui/material';
import { useTranslations } from 'next-intl';

const tilat = (t: typeof useTranslations) => {
  return TILAT.map((tila) => {
    return {
      value: tila,
      label: t(`yleinen.tila.${tila}`),
    };
  });
};

export const KoulutuksenTila = () => {
  const t = useTranslations();
  const { setSelectedKoulutuksenTila } = useSearchParams();

  const changeTila = (e: SelectChangeEvent) => {
    return setSelectedKoulutuksenTila(e.target.value);
  };

  const id = 'koulutuksen-tila';

  return (
    <LocalizedSelect
      id={id}
      label={t(`raportti.${id}`)}
      options={tilat(t)}
      onChange={changeTila}
    />
  );
};

export const ToteutuksenTila = () => {
  const t = useTranslations();
  const { setSelectedToteutuksenTila } = useSearchParams();

  const changeTila = (e: SelectChangeEvent) => {
    return setSelectedToteutuksenTila(e.target.value);
  };

  const id = 'toteutuksen-tila';

  return (
    <LocalizedSelect
      id={id}
      label={t(`raportti.${id}`)}
      options={tilat(t)}
      onChange={changeTila}
    />
  );
};

export const HakukohteenTila = () => {
  const t = useTranslations();
  const { setSelectedHakukohteenTila } = useSearchParams();

  const changeTila = (e: SelectChangeEvent) => {
    return setSelectedHakukohteenTila(e.target.value);
  };

  const id = 'hakukohteen-tila';

  return (
    <LocalizedSelect
      id={id}
      label={t(`raportti.${id}`)}
      options={tilat(t)}
      onChange={changeTila}
    />
  );
};
