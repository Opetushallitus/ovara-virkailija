import { useTranslations } from 'next-intl';
import { useSearchParams } from '@/app/hooks/useSearchParams';
import {
  Box,
  Radio,
  RadioGroup,
  FormControlLabel,
  SelectChangeEvent,
} from '@mui/material';
import { OvaraFormControl } from '@/app/components/form/ovara-form-control';
import { match } from 'ts-pattern';

type RadioGroupProps = {
  onChange: (e: SelectChangeEvent) => void;
  options: Array<string>;
  label: string;
  value: string;
  required?: boolean;
};

const OvaraRadioGroup = ({
  onChange,
  options,
  label,
  value,
  required,
}: RadioGroupProps) => {
  const t = useTranslations();

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
                    label={t(`raportti.radio-group.${option}`)}
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

export const Valintakoe = () => {
  const t = useTranslations();

  const RADIOGROUP_OPTIONS = ['neutral', 'yes', 'no'];

  const { selectedValintakoe, setSelectedValintakoe } = useSearchParams();

  const changeValintakoeSelection = (e: SelectChangeEvent) => {
    const value = e.target.value;
    if (value === 'no') {
      return setSelectedValintakoe(false);
    } else if (value === 'yes') {
      return setSelectedValintakoe(true);
    } else {
      return setSelectedValintakoe(null);
    }
  };

  const selected = match(selectedValintakoe)
    .with(true, () => 'yes')
    .with(false, () => 'no')
    .otherwise(() => 'neutral');

  return (
    <OvaraRadioGroup
      label={t(`raportti.valintakoe`)}
      options={RADIOGROUP_OPTIONS}
      value={selected}
      onChange={changeValintakoeSelection}
    />
  );
};
