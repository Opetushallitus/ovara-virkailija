import { useTranslations } from 'next-intl';
import { Autocomplete, Chip, TextField } from '@mui/material';
import { OvaraFormControl } from '@/app/components/form/ovara-form-control';

export type SelectOption = {
  label: string;
  value: string;
};

type MultiComboBoxProps = {
  options: Array<SelectOption>;
  onChange: (e: React.SyntheticEvent, value: Array<SelectOption>) => void;
  id: string;
  label: string;
  value: Array<string>;
  required?: boolean;
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
      renderInput={() => (
        <Autocomplete<SelectOption, true, true, false>
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
          renderTags={(value: Array<SelectOption>, getTagProps) => {
            return value?.map((option: SelectOption, index: number) => {
              const { key, ...tagProps } = getTagProps({ index });
              return <Chip label={option?.label} key={key} {...tagProps} />;
            });
          }}
          renderInput={(params) => (
            <TextField {...params} placeholder={t('yleinen.valitse')} />
          )}
        />
      )}
    />
  );
};
