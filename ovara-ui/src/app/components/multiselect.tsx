'use client';
import { OphMultiSelect, OphSelectOption } from './OphMultiSelect';
import { useTranslations } from 'next-intl';
import { Box, Chip, SelectChangeEvent } from '@mui/material';
import { useSearchParams } from '@/app/hooks/useSearchParams';
import { find } from 'remeda';

export const MultiSelect = (
  props: React.ComponentProps<typeof OphMultiSelect>,
) => {
  const t = useTranslations();

  const { options } = props;
  const { selectedAlkamiskaudet, setSelectedAlkamiskaudet } = useSearchParams();

  const findLabel = (options: Array<OphSelectOption>, value: string) => {
    return find(options, (option) => option.value === value)?.label;
  };

  const onDelete = (
    _: SelectChangeEvent,
    valueToRemove: string,
    selected: Array<string> | null,
  ) => {
    const updatedSelected = selected?.filter(
      (selectedValue) => selectedValue !== valueToRemove,
    );
    setSelectedAlkamiskaudet(updatedSelected || []);
  };

  return (
    <OphMultiSelect
      inputProps={{ 'aria-label': t('yleinen.valitsevaihtoehto') }}
      placeholder={t('yleinen.valitse')}
      renderValue={(selected) => (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
          {selected.map((value) => (
            <Chip
              sx={{ borderRadius: 'unset', fontSize: '1rem' }}
              key={value}
              label={findLabel(options, value)}
              onDelete={(e) => onDelete(e, value, selectedAlkamiskaudet)}
              onMouseDown={(event) => {
                event.stopPropagation();
              }}
            />
          ))}
        </Box>
      )}
      multiple
      {...props}
    />
  );
};
