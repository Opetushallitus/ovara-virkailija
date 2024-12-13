import { useTranslations } from 'next-intl';
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
  const t = useTranslations();

  const getValueFromOptions = (value: Array<string>) => {
    return (
      value
        ?.map((v) => {
          return find(options, (o) => o.value === v);
        })
        // filter undefined values
        .filter((v) => !!v)
    );
  };

  return (
    <OvaraFormControl
      label={required ? `${label} *` : label}
      renderInput={() => (
        <Autocomplete
          multiple
          id={id}
          sx={{ width: '100%', overflow: 'hidden' }}
          onChange={onChange}
          value={value ? getValueFromOptions(value) : []}
          options={options}
          filterSelectedOptions
          isOptionEqualToValue={(option, selected) => {
            return option.value === selected.value;
          }}
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

type ComboBoxProps = {
  options: Array<SelectOption>;
  onChange: (e: React.SyntheticEvent, value: SelectOption | null) => void;
  id: string;
  label: string;
  value?: string;
  required?: boolean;
};

export const ComboBox = ({
  options,
  onChange,
  id,
  label,
  value,
  required,
}: ComboBoxProps) => {
  const t = useTranslations();

  const getValueFromOptions = (value: string | undefined) => {
    return find(options, (o) => o.value === value);
  };

  return (
    <OvaraFormControl
      label={required ? `${label} *` : label}
      renderInput={() => (
        <Autocomplete
          id={id}
          sx={{ width: '100%', overflow: 'hidden' }}
          value={getValueFromOptions(value) ?? null}
          onChange={onChange}
          options={options}
          getOptionKey={(option) => option.value}
          renderInput={(params) => (
            <TextField {...params} placeholder={t('yleinen.valitse')} />
          )}
        />
      )}
    />
  );
};
