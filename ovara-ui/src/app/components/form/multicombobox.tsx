import { useTranslations } from 'next-intl';
import { Autocomplete, Chip, TextField } from '@mui/material';
import { OvaraFormControl } from '@/app/components/form/ovara-form-control';

export type OphMultiComboBoxOption = {
  label?: string;
  value: string;
};

type MultiComboBoxProps = {
  options: Array<OphMultiComboBoxOption>;
  onChange: (
    e: React.SyntheticEvent,
    value: Array<OphMultiComboBoxOption>,
  ) => void;
  id: string;
  label: string;
  required: boolean;
};

export const MultiComboBox = ({
  options,
  onChange,
  id,
  label,
  required,
}: MultiComboBoxProps) => {
  const t = useTranslations();
  return (
    <OvaraFormControl
      label={required ? `${label} *` : label}
      sx={{ textAlign: 'left', flex: '1 0 180px' }}
      renderInput={() => (
        <Autocomplete<OphMultiComboBoxOption, true, true, false>
          multiple
          id={id}
          sx={{ width: '100%' }}
          onChange={onChange}
          options={options}
          filterSelectedOptions
          isOptionEqualToValue={(option, selected) =>
            option.value === selected.value
          }
          getOptionKey={(option) => option.value}
          renderTags={(value: Array<OphMultiComboBoxOption>, getTagProps) => {
            return value?.map(
              (option: OphMultiComboBoxOption, index: number) => {
                const { key, ...tagProps } = getTagProps({ index });
                return <Chip label={option?.label} key={key} {...tagProps} />;
              },
            );
          }}
          renderInput={(params) => (
            <TextField {...params} placeholder={t('yleinen.valitse')} />
          )}
        />
      )}
    />
  );
};
