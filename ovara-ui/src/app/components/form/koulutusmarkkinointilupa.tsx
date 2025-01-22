import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { SelectChangeEvent } from '@mui/material';
import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { match } from 'ts-pattern';
import { RADIOGROUP_OPTIONS } from '@/app/lib/constants';

export const Markkinointilupa = ({ t }: { t: (key: string) => string }) => {
  const { selectedMarkkinointilupa, setSelectedMarkkinointilupa } =
    useHakijatSearchParams();

  const changeMarkkinointilupaSelection = (e: SelectChangeEvent) => {
    const value = e.target.value;
    if (value === 'no') {
      return setSelectedMarkkinointilupa(false);
    } else if (value === 'yes') {
      return setSelectedMarkkinointilupa(true);
    } else {
      return setSelectedMarkkinointilupa(null);
    }
  };

  const selected = match(selectedMarkkinointilupa)
    .with(true, () => 'yes')
    .with(false, () => 'no')
    .otherwise(() => 'neutral');

  return (
    <OvaraRadioGroup
      label={t(`raportti.markkinointilupa`)}
      options={RADIOGROUP_OPTIONS}
      value={selected}
      onChange={changeMarkkinointilupaSelection}
    />
  );
};
