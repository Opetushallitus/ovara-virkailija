'use client';
import { OphSelect } from '@opetushallitus/oph-design-system';
import { useTranslate } from '@tolgee/react';
import { OvaraFormControl } from './ovara-form-control';
import { SelectOption } from './multicombobox';
import { SelectChangeEvent } from '@mui/material';

type LocalizedSelectProps = {
  id: string;
  label: string;
  options: Array<SelectOption>;
  onChange: (e: SelectChangeEvent) => void;
  required?: boolean;
  value: string | undefined;
};

export const LocalizedSelect = (props: LocalizedSelectProps) => {
  const { t } = useTranslate();
  const { required, label } = props;

  return (
    <OvaraFormControl
      sx={{ padding: '5px' }}
      label={required ? `${label} *` : label}
      renderInput={() => (
        <OphSelect
          sx={{ width: '100%' }}
          inputProps={{ 'aria-label': t('yleinen.valitsevaihtoehto') }}
          placeholder={t('yleinen.valitse')}
          clearable
          {...props}
        />
      )}
    />
  );
};
