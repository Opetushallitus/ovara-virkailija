import { useSearchParams } from '@/app/hooks/useSearchParams';
import { LocalizedSelect } from './localized-select';
import { TILAT } from '@/app/lib/constants';
import { SelectChangeEvent } from '@mui/material';
import { useTranslations } from 'next-intl';
import { isEmpty } from 'remeda';

const tilat = (t: typeof useTranslations) => {
  return TILAT.map((tila) => {
    return {
      value: tila,
      label: t(`yleinen.tila.${tila}`),
    };
  });
};

const changeTila = (
  e: SelectChangeEvent,
  changeFn: (v: string | null) => void,
) => {
  const value = e.target.value;
  if (isEmpty(value)) {
    return changeFn(null);
  }
  return changeFn(value);
};

export const KoulutuksenTila = () => {
  const t = useTranslations();
  const { selectedKoulutuksenTila, setSelectedKoulutuksenTila } =
    useSearchParams();

  const id = 'koulutuksen-tila';

  return (
    <LocalizedSelect
      id={id}
      label={t(`raportti.${id}`)}
      value={selectedKoulutuksenTila ?? ''}
      options={tilat(t)}
      onChange={(e) => changeTila(e, setSelectedKoulutuksenTila)}
    />
  );
};

export const ToteutuksenTila = () => {
  const t = useTranslations();
  const { selectedToteutuksenTila, setSelectedToteutuksenTila } =
    useSearchParams();

  const id = 'toteutuksen-tila';

  return (
    <LocalizedSelect
      id={id}
      label={t(`raportti.${id}`)}
      value={selectedToteutuksenTila ?? ''}
      options={tilat(t)}
      onChange={(e) => changeTila(e, setSelectedToteutuksenTila)}
    />
  );
};

export const HakukohteenTila = () => {
  const t = useTranslations();
  const { selectedHakukohteenTila, setSelectedHakukohteenTila } =
    useSearchParams();

  const id = 'hakukohteen-tila';

  return (
    <LocalizedSelect
      id={id}
      label={t(`raportti.${id}`)}
      value={selectedHakukohteenTila ?? ''}
      options={tilat(t)}
      onChange={(e) => changeTila(e, setSelectedHakukohteenTila)}
    />
  );
};
