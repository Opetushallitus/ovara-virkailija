import { useTranslate } from '@tolgee/react';
import {
  Box,
  Radio,
  RadioGroup,
  FormControlLabel,
  SelectChangeEvent,
} from '@mui/material';
import { OvaraFormControl } from '@/app/components/form/ovara-form-control';

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

  return (
    <Box>
      {options && (
        <OvaraFormControl
          label={required ? `${label} *` : label}
          renderInput={() => (
            <RadioGroup
              sx={{ width: '100%' }}
              row
              aria-labelledby={label}
              defaultValue={options ? options[0] : null}
              onChange={onChange}
              value={value}
            >
              {options?.map((option) => {
                return (
                  <FormControlLabel
                    key={option}
                    value={option}
                    control={<Radio />}
                    label={
                      labels
                        ? labels[option]
                        : t(`raportti.radio-group.${option}`)
                    }
                  />
                );
              })}
            </RadioGroup>
          )}
        />
      )}
    </Box>
  );
};
