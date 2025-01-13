import { useTranslate } from '@tolgee/react';
import { Autocomplete, Chip, TextField } from '@mui/material';
import { OvaraFormControl } from '@/app/components/form/ovara-form-control';
import { find } from 'remeda';

export type SelectOption = {
  label: string;
  value: string;
};

type MultiComboBoxProps = {
  options: Array<SelectOption>;
  onChange: (e: React.SyntheticEvent, value: Array<SelectOption>) => void;
  id: string;
  label: string;
  value: Array<string> | undefined;
  required?: boolean;
};

export const MultiComboBox = ({
  options,
  onChange,
  id,
  label,
  value,
  required,
}: MultiComboBoxProps) => {
  const { t } = useTranslate();

  const getValueFromOptions = (value: Array<string>) => {
    return value.flatMap((v) => {
      const match = find(options, (o) => o.value === v);
      return match ? [match] : [];
    });
  };

  return (
    <OvaraFormControl
      label={required ? `${label} *` : label}
      renderInput={() => (
        <Autocomplete<SelectOption, true, true, false>
          multiple
          id={id}
          sx={{ width: '100%', overflow: 'hidden' }}
          onChange={onChange}
          value={value ? getValueFromOptions(value) : []}
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
