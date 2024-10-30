import { useTranslations } from 'next-intl';
import { useSearchParams } from '@/app/hooks/useSearchParams';
import { LocalizedSelect } from './localized-select';
import { TILAT } from '@/app/lib/constants';

const tilat = (t) => {
  return TILAT.map((tila) => {
    return {
      value: tila,
      label: t(`yleinen.tila.${tila}`),
    };
  });
};

export const KoulutuksenTila = ({ t }: { t: typeof useTranslations }) => {
  const { setSelectedKoulutuksenTila } = useSearchParams();

  const changeTila = (e: React.SyntheticEvent) => {
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

export const ToteutuksenTila = ({ t }: { t: typeof useTranslations }) => {
  const { setSelectedToteutuksenTila } = useSearchParams();

  const changeTila = (e: React.SyntheticEvent) => {
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

export const HakukohteenTila = ({ t }: { t: typeof useTranslations }) => {
  const { setSelectedHakukohteenTila } = useSearchParams();

  const changeTila = (e: React.SyntheticEvent) => {
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
