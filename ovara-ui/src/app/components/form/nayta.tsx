import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { RADIOGROUP_BOOLEAN_OPTIONS } from '@/app/lib/constants';
import {
  changeRadioGroupSelection,
  getSelectedRadioGroupValue,
} from '@/app/components/form/utils';

export const NaytaYoArvosanat = ({ t }: { t: (key: string) => string }) => {
  const { selectedNaytaYoArvosanat, setSelectedNaytaYoArvosanat } =
    useHakijatSearchParams();

  return (
    <OvaraRadioGroup
      label={t(`raportti.nayta-yo-arvosanat`)}
      options={RADIOGROUP_BOOLEAN_OPTIONS}
      value={getSelectedRadioGroupValue(selectedNaytaYoArvosanat)}
      onChange={(e) =>
        changeRadioGroupSelection(e, setSelectedNaytaYoArvosanat)
      }
    />
  );
};

export const NaytaHetu = ({ t }: { t: (key: string) => string }) => {
  const { selectedNaytaHetu, setSelectedNaytaHetu } = useHakijatSearchParams();

  return (
    <OvaraRadioGroup
      label={t(`raportti.nayta-hetu`)}
      options={RADIOGROUP_BOOLEAN_OPTIONS}
      value={getSelectedRadioGroupValue(selectedNaytaHetu)}
      onChange={(e) => changeRadioGroupSelection(e, setSelectedNaytaHetu)}
    />
  );
};

export const NaytaPostiosoite = ({ t }: { t: (key: string) => string }) => {
  const { selectedNaytaPostiosoite, setSelectedNaytaPostiosoite } =
    useHakijatSearchParams();

  return (
    <OvaraRadioGroup
      label={t(`raportti.nayta-postiosoite`)}
      options={RADIOGROUP_BOOLEAN_OPTIONS}
      value={getSelectedRadioGroupValue(selectedNaytaPostiosoite)}
      onChange={(e) =>
        changeRadioGroupSelection(e, setSelectedNaytaPostiosoite)
      }
    />
  );
};
