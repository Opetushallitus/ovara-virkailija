import { useTranslate } from '@tolgee/react';
import { useSearchParams } from '@/app/hooks/useSearchParams';
import { SelectChangeEvent } from '@mui/material';
import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { match } from 'ts-pattern';

export const Valintakoe = () => {
  const { t } = useTranslate();

  const RADIOGROUP_OPTIONS = ['neutral', 'yes', 'no'];

  const { selectedValintakoe, setSelectedValintakoe } = useSearchParams();

  const changeValintakoeSelection = (e: SelectChangeEvent) => {
    const value = e.target.value;
    if (value === 'no') {
      return setSelectedValintakoe(false);
    } else if (value === 'yes') {
      return setSelectedValintakoe(true);
    } else {
      return setSelectedValintakoe(null);
    }
  };

  const selected = match(selectedValintakoe)
    .with(true, () => 'yes')
    .with(false, () => 'no')
    .otherwise(() => 'neutral');

  return (
    <OvaraRadioGroup
      label={t(`raportti.valintakoe`)}
      options={RADIOGROUP_OPTIONS}
      value={selected}
      onChange={changeValintakoeSelection}
    />
  );
};
