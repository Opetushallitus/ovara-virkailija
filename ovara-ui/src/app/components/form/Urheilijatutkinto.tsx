import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { RADIOGROUP_OPTIONS } from '@/app/lib/constants';
import {
  changeRadioGroupSelection,
  getSelectedRadioGroupValue,
} from '@/app/components/form/utils';

export const Urheilijatutkinto = ({ t }: { t: (key: string) => string }) => {
  const { selectedUrheilijatutkinto, setSelectedUrheilijatutkinto } =
    useHakijatSearchParams();

  return (
    <OvaraRadioGroup
      label={t(`raportti.urheilijatutkintoKiinnostaa`)}
      options={RADIOGROUP_OPTIONS}
      value={getSelectedRadioGroupValue(selectedUrheilijatutkinto)}
      onChange={(e) =>
        changeRadioGroupSelection(e, setSelectedUrheilijatutkinto)
      }
    />
  );
};
