import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { RADIOGROUP_OPTIONS } from '@/app/lib/constants';
import {
  changeRadioGroupSelection,
  getSelectedRadioGroupValue,
} from '@/app/components/form/utils';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';
import { useTranslate } from '@tolgee/react';

export const Ensikertalainen = () => {
  const { t } = useTranslate();
  const { selectedEnsikertalainen, setSelectedEnsikertalainen } =
    useHakeneetSearchParams();

  return (
    <OvaraRadioGroup
      label={t(`raportti.ensikertalainen`)}
      options={RADIOGROUP_OPTIONS}
      value={getSelectedRadioGroupValue(selectedEnsikertalainen)}
      onChange={(e) => changeRadioGroupSelection(e, setSelectedEnsikertalainen)}
    />
  );
};
