import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { RADIOGROUP_BOOLEAN_OPTIONS } from '@/app/lib/constants';
import {
  changeRadioGroupSelection,
  getSelectedRadioGroupValue,
} from '@/app/components/form/utils';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';
import { useTranslate } from '@tolgee/react';

export const NaytaHakutoiveet = () => {
  const { t } = useTranslate();
  const { selectedNaytaHakutoiveet, setSelectedNaytaHakutoiveet } =
    useHakeneetSearchParams();

  return (
    <OvaraRadioGroup
      label={t(`raportti.nayta-hakutoiveet`)}
      options={RADIOGROUP_BOOLEAN_OPTIONS}
      value={getSelectedRadioGroupValue(selectedNaytaHakutoiveet)}
      onChange={(e) =>
        changeRadioGroupSelection(e, setSelectedNaytaHakutoiveet)
      }
    />
  );
};
