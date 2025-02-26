import { Box } from '@mui/material';
import { OphCheckbox } from '@opetushallitus/oph-design-system';
import { OvaraFormControl } from '@/app/components/form/ovara-form-control';
import { isChecked, changeChecked } from '@/app/components/form/utils';

type TranslationFunction = (key: string) => string;

export const OvaraCheckboxGroup = ({
  id,
  options,
  selectedValues,
  setSelectedValues,
  t,
  getTranslation,
}: {
  id: string;
  options: Array<string>;
  selectedValues: Array<string> | null;
  setSelectedValues: (v: Array<string> | null) => void;
  t: TranslationFunction;
  getTranslation?: (s: string, t: TranslationFunction) => string;
}) => {
  return (
    <OvaraFormControl
      sx={{
        marginBottom: '20px',
        display: 'flex',
      }}
      label={t(`raportti.${id}`)}
      renderInput={() => (
        <Box
          sx={{
            columns: 2,
            width: '100%',
          }}
        >
          {options?.map((option: string) => {
            return (
              <Box key={option}>
                <OphCheckbox
                  id={option}
                  checked={isChecked(option, selectedValues)}
                  onChange={(e) =>
                    changeChecked(e, option, selectedValues, setSelectedValues)
                  }
                  label={
                    getTranslation
                      ? getTranslation(option, t)
                      : t(`raportti.${option.toLowerCase()}`)
                  }
                />
              </Box>
            );
          })}
        </Box>
      )}
    />
  );
};
