import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { YES_NO_RADIOGROUP_OPTIONS } from '@/app/lib/constants';
import {
  changeRadioGroupSelection,
  getSelectedRadioGroupValue,
} from '@/app/components/form/utils';

export const NaytaYoArvosanat = ({ t }: { t: (key: string) => string }) => {
  const { selectedYoArvosanat, setSelectedYoArvosanat } =
    useHakijatSearchParams();

  return (
    <OvaraRadioGroup
      label={t(`raportti.nayta-yo-arvosanat`)}
      options={YES_NO_RADIOGROUP_OPTIONS}
      value={getSelectedRadioGroupValue(selectedYoArvosanat)}
      onChange={(e) => changeRadioGroupSelection(e, setSelectedYoArvosanat)}
    />
  );
};
