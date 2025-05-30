import { useTranslate } from '@tolgee/react';
import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { RADIOGROUP_OPTIONS } from '@/app/lib/constants';
import {
  changeRadioGroupSelection,
  getSelectedRadioGroupValue,
} from '@/app/components/form/utils';

export const Markkinointilupa = () => {
  const { t } = useTranslate();

  const { selectedMarkkinointilupa, setSelectedMarkkinointilupa } =
    useHakijatSearchParams();

  return (
    <OvaraRadioGroup
      label={t(`raportti.markkinointilupa`)}
      options={RADIOGROUP_OPTIONS}
      value={getSelectedRadioGroupValue(selectedMarkkinointilupa)}
      onChange={(e) =>
        changeRadioGroupSelection(e, setSelectedMarkkinointilupa)
      }
    />
  );
};
