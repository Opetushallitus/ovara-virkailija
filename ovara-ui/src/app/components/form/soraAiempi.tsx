import { useTranslate } from '@tolgee/react';
import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { RADIOGROUP_OPTIONS } from '@/app/lib/constants';
import {
  changeRadioGroupSelection,
  getSelectedRadioGroupValue,
} from '@/app/components/form/utils';

export const SoraAiempi = () => {
  const { t } = useTranslate();

  const { selectedSoraAiempi, setSelectedSoraAiempi } =
    useHakijatSearchParams();

  return (
    <OvaraRadioGroup
      label={t(`raportti.soraAiempi`)}
      options={RADIOGROUP_OPTIONS}
      value={getSelectedRadioGroupValue(selectedSoraAiempi)}
      onChange={(e) => changeRadioGroupSelection(e, setSelectedSoraAiempi)}
    />
  );
};
