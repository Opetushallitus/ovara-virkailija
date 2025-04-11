import { useTranslate } from '@tolgee/react';
import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { RADIOGROUP_OPTIONS } from '@/app/lib/constants';
import {
  changeRadioGroupSelection,
  getSelectedRadioGroupValue,
} from '@/app/components/form/utils';

export const SoraTerveys = () => {
  const { t } = useTranslate();

  const { selectedSoraTerveys, setSelectedSoraTerveys } =
    useHakijatSearchParams();

  return (
    <OvaraRadioGroup
      label={t(`raportti.soraTerveys`)}
      options={RADIOGROUP_OPTIONS}
      value={getSelectedRadioGroupValue(selectedSoraTerveys)}
      onChange={(e) => changeRadioGroupSelection(e, setSelectedSoraTerveys)}
    />
  );
};
