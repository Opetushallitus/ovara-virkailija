import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { RADIOGROUP_OPTIONS } from '@/app/lib/constants';
import {
  changeRadioGroupSelection,
  getSelectedRadioGroupValue,
} from '@/app/components/form/utils';

export const Kaksoistutkinto = ({ t }: { t: (key: string) => string }) => {
  const { selectedKaksoistutkinto, setSelectedKaksoistutkinto } =
    useHakijatSearchParams();

  return (
    <OvaraRadioGroup
      label={t(`raportti.kaksoistutkintoKiinnostaa`)}
      options={RADIOGROUP_OPTIONS}
      value={getSelectedRadioGroupValue(selectedKaksoistutkinto)}
      onChange={(e) => changeRadioGroupSelection(e, setSelectedKaksoistutkinto)}
    />
  );
};
