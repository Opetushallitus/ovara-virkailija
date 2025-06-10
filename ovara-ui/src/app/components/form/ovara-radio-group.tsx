import { useTranslate } from '@tolgee/react';
import { Box, SelectChangeEvent } from '@mui/material';
import { OvaraFormControl } from '@/app/components/form/ovara-form-control';
import { OphRadioGroup } from '@opetushallitus/oph-design-system';

// TODO importoi oph-design-systemistä sitten kun on exportoitu siellä
interface OphRadioOption<T> {
  value: T;
  label: string;
}

type RadioGroupProps = {
  onChange: (e: SelectChangeEvent) => void;
  options: Array<string>;
  label: string;
  value: string;
  required?: boolean;
  labels?: { [key: string]: string };
};

export const OvaraRadioGroup = ({
  onChange,
  options,
  label,
  value,
  required,
  labels,
}: RadioGroupProps) => {
  const { t } = useTranslate();

  const radioOptions: OphRadioOption<string>[] = options.map((option) => ({
    value: option,
    label: labels ? labels[option] : t(`raportti.radio-group.${option}`),
  }));

  return (
    <Box>
      {options && (
        <OvaraFormControl
          label={required ? `${label} *` : label}
          renderInput={({ labelId }) => (
            <OphRadioGroup
              sx={{ width: '100%' }}
              options={radioOptions}
              row
              defaultValue={options ? options[0] : null}
              onChange={onChange}
              value={value}
              labelId={labelId}
            />
          )}
        />
      )}
    </Box>
  );
};
