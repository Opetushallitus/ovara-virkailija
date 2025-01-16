import { useSearchParams } from '@/app/hooks/useSearchParams';
import { LocalizedSelect } from './localized-select';
import { TILAT } from '@/app/lib/constants';
import { SelectChangeEvent } from '@mui/material';
import { useTranslate } from '@tolgee/react';
import { isEmpty } from 'remeda';

const tilat = (t: (key: string) => string) => {
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
  const { t } = useTranslate();
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
  const { t } = useTranslate();
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
  const { t } = useTranslate();
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
