'use client';
import { OphSelect } from '@opetushallitus/oph-design-system';
import { useTranslations } from 'next-intl';
import { OvaraFormControl } from './ovara-form-control';

export const LocalizedSelect = (
  props: React.ComponentProps<typeof OphSelect>,
) => {
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
          {...props}
        />
      )}
    />
  );
};
