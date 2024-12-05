'use client';
import { OphSelect } from '@opetushallitus/oph-design-system';
import { useTranslations } from 'next-intl';
import { OvaraFormControl } from './ovara-form-control';
import { SelectOption } from './multicombobox';
import { SelectChangeEvent } from '@mui/material';

type LocalizedSelectProps = {
  id: string;
  label: string;
  options: Array<SelectOption>;
  onChange: (e: SelectChangeEvent) => void;
  required?: boolean;
};

export const LocalizedSelect = (props: LocalizedSelectProps) => {
  const t = useTranslations();
  const { required, label } = props;

  return (
    <OvaraFormControl
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
