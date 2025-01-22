import { useTranslate } from '@tolgee/react';
import { useSearchParams } from '@/app/hooks/searchParams/useSearchParams';
import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { RADIOGROUP_OPTIONS } from '@/app/lib/constants';
import {
  changeRadioGroupSelection,
  getSelectedRadioGroupValue,
} from '@/app/components/form/utils';

export const Valintakoe = () => {
  const { t } = useTranslate();

  const { selectedValintakoe, setSelectedValintakoe } = useSearchParams();

  return (
    <OvaraRadioGroup
      label={t(`raportti.valintakoe`)}
      options={RADIOGROUP_OPTIONS}
      value={getSelectedRadioGroupValue(selectedValintakoe)}
      onChange={(e) => changeRadioGroupSelection(e, setSelectedValintakoe)}
    />
  );
};
