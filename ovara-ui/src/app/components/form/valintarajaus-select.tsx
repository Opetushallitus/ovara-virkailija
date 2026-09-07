import { ComboBox, SelectOption } from '@/app/components/form/multicombobox';
import { useTranslate } from '@tolgee/react';
import { isNullish } from 'remeda';

export const VALINTARAJAUS_VALUES = [
  'HAKENEET',
  'HYVAKSYTYT',
  'VASTAANOTTANEET',
] as const;

export type Valintarajaus = (typeof VALINTARAJAUS_VALUES)[number];

type Props = {
  value: string | null;
  onChange: (value: string | null) => void;
  required?: boolean;
};

export const ValintarajausSelect = ({ value, onChange, required }: Props) => {
  const { t } = useTranslate();

  const options: SelectOption[] = VALINTARAJAUS_VALUES.map((v) => ({
    value: v,
    label: t(`raportti.valintarajaus.${v.toLowerCase()}`),
  }));

  return (
    <ComboBox
      id="valintarajaus-select"
      label={t('raportti.valintarajaus')}
      value={value ?? undefined}
      options={options}
      onChange={(_, selected) =>
        onChange(isNullish(selected) ? null : selected.value)
      }
      required={required}
      sortOptions={false}
    />
  );
};
