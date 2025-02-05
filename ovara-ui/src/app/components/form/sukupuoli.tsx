import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { SUKUPUOLET } from '@/app/lib/constants';
import {
  changeRadioGroupSelection,
  getSelectedRadioGroupValue,
} from '@/app/components/form/utils';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';

export const Sukupuoli = ({ t }: { t: (key: string) => string }) => {
  const { selectedSukupuoli, setSelectedSukupuoli } = useHakeneetSearchParams();

  return (
    <OvaraRadioGroup
      label={t(`raportti.nayta-hakutoiveet`)}
      options={SUKUPUOLET}
      value={getSelectedRadioGroupValue(selectedSukupuoli)}
      onChange={(e) => changeRadioGroupSelection(e, setSelectedSukupuoli)}
    />
  );
};
