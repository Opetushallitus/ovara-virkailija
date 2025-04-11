import { useTranslate } from '@tolgee/react';
import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { RADIOGROUP_OPTIONS } from '@/app/lib/constants';
import {
  changeRadioGroupSelection,
  getSelectedRadioGroupValue,
} from '@/app/components/form/utils';

export const Julkaisulupa = () => {
  const { t } = useTranslate();

  const { selectedJulkaisulupa, setSelectedJulkaisulupa } =
    useHakijatSearchParams();

  return (
    <OvaraRadioGroup
      label={t(`raportti.julkaisulupa`)}
      options={RADIOGROUP_OPTIONS}
      value={getSelectedRadioGroupValue(selectedJulkaisulupa)}
      onChange={(e) => changeRadioGroupSelection(e, setSelectedJulkaisulupa)}
    />
  );
};
