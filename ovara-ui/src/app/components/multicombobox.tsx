import { useTranslations } from 'next-intl';
import { Autocomplete, Chip, TextField } from '@mui/material';

export type OphMultiComboBoxOption = {
  label: string;
  value: string;
};

type MultiComboBoxProps = {
  options: Array<OphMultiComboBoxOption>;
  onChange: (
    e: React.SyntheticEvent,
    value: Array<OphMultiComboBoxOption>,
  ) => void;
  id: string;
};

export const MultiComboBox = ({
  options,
  onChange,
  id,
}: MultiComboBoxProps) => {
  const t = useTranslations();
  return (
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
      renderTags={(value: Array<OphMultiComboBoxOption>, getTagProps) => {
        return value?.map((option: OphMultiComboBoxOption, index: number) => {
          const { key, ...tagProps } = getTagProps({ index });
          return <Chip label={option?.label} key={key} {...tagProps} />;
        });
      }}
      renderInput={(params) => (
        <TextField {...params} placeholder={t('yleinen.valitse')} />
      )}
    />
  );
};