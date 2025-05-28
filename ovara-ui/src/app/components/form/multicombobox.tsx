import { useTranslate } from '@tolgee/react';
import { Autocomplete, Chip, TextField } from '@mui/material';
import { OvaraFormControl } from '@/app/components/form/ovara-form-control';
import { find, isEmpty } from 'remeda';
import { LanguageCode } from '@/app/lib/types/common';
import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';

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
  sortOptions?: boolean;
  sx?: Record<string, string | number>;
};

export const MultiComboBox = ({
  options,
  onChange,
  id,
  label,
  value,
  required,
  sortOptions = true,
  ...props
}: MultiComboBoxProps) => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli ?? 'fi') as LanguageCode;

  const getValueFromOptions = (value: Array<string>) => {
    return value.flatMap((v) => {
      const match = find(options, (o) => o.value === v);
      return match ? [match] : [];
    });
  };

  const sortedOptions = sortOptions
    ? [...options].sort((a, b) =>
        new Intl.Collator(locale).compare(a.label, b.label),
      )
    : options;

  return (
    <OvaraFormControl
      {...props}
      label={required ? `${label} *` : label}
      renderInput={() => (
        <Autocomplete
          multiple
          id={id}
          sx={{ width: '100%', overflow: 'hidden' }}
          onChange={onChange}
          value={value ? getValueFromOptions(value) : []}
          options={sortedOptions}
          filterSelectedOptions
          isOptionEqualToValue={(option, selected) => {
            return option.value === selected.value;
          }}
          getOptionKey={(option) => option.value}
          renderTags={(value: Array<SelectOption>, getTagProps) => {
            return value?.map((option: SelectOption, index: number) => {
              const { key, ...tagProps } = getTagProps({ index });
              return (
                <Chip
                  size="small"
                  label={option?.label}
                  key={key}
                  {...tagProps}
                />
              );
            });
          }}
          renderInput={(params) => (
            <TextField {...params} placeholder={t('yleinen.valitse')} />
          )}
          disabled={options && isEmpty(options)}
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
  sortOptions?: boolean;
};

export const ComboBox = ({
  options,
  onChange,
  id,
  label,
  value,
  required,
  sortOptions = true,
}: ComboBoxProps) => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli ?? 'fi') as LanguageCode;

  const getValueFromOptions = (value: string | undefined) => {
    return find(options, (o) => o.value === value);
  };

  const sortedOptions = sortOptions
    ? [...options].sort((a, b) =>
        new Intl.Collator(locale).compare(a.label, b.label),
      )
    : options;

  return (
    <OvaraFormControl
      label={required ? `${label} *` : label}
      renderInput={() => (
        <Autocomplete
          id={id}
          sx={{ width: '100%', overflow: 'hidden' }}
          value={getValueFromOptions(value) ?? null}
          onChange={onChange}
          options={sortedOptions}
          getOptionKey={(option) => option.value}
          renderInput={(params) => (
            <TextField {...params} placeholder={t('yleinen.valitse')} />
          )}
          disabled={options && isEmpty(options)}
        />
      )}
    />
  );
};
