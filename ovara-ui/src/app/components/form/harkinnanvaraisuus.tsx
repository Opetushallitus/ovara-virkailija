import { useSearchParams } from '@/app/hooks/searchParams/useSearchParams';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { Box, SelectChangeEvent } from '@mui/material';
import { OphCheckbox } from '@opetushallitus/oph-design-system';
import { OvaraFormControl } from '@/app/components/form/ovara-form-control';
import { isEmpty, isNullish } from 'remeda';

export const Harkinnanvaraisuus = ({ t }: { t: (key: string) => string }) => {
  const { data: harkinnanvaraisuudet } = useQuery({
    queryKey: ['fetchHarkinnanvaraisuudet'],
    queryFn: () => doApiFetch('harkinnanvaraisuudet'),
  });

  const { selectedHarkinnanvaraisuus, setSelectedHarkinnanvaraisuus } =
    useSearchParams();

  const includesValue = (id: string, selectedValues: Array<string> | null) => {
    return isNullish(selectedValues) ? false : selectedValues?.includes(id);
  };

  const isChecked = (harkinnanvaraisuus: string) => {
    return includesValue(harkinnanvaraisuus, selectedHarkinnanvaraisuus);
  };

  const changeChecked = (
    _: SelectChangeEvent,
    id: string,
    selected: Array<string> | null,
    setSelected: (v: Array<string> | null) => void,
  ) => {
    let newValue = null;

    if (includesValue(id, selected)) {
      newValue = selected?.filter((value) => id !== value) || null;
    } else {
      if (isNullish(selected)) {
        newValue = [id];
      } else {
        newValue = selected?.concat([id]);
      }
    }

    setSelected(isNullish(newValue) || isEmpty(newValue) ? null : newValue);
  };

  return (
    <OvaraFormControl
      sx={{
        marginBottom: '20px',
        display: 'flex',
      }}
      label={t(`raportti.harkinnanvaraisuus`)}
      renderInput={() => (
        <Box
          sx={{
            columns: 2,
          }}
        >
          {harkinnanvaraisuudet?.map((harkinnanvaraisuus: string) => {
            return (
              <Box key={harkinnanvaraisuus}>
                <OphCheckbox
                  id={harkinnanvaraisuus}
                  checked={isChecked(harkinnanvaraisuus)}
                  onChange={(e) =>
                    changeChecked(
                      e,
                      harkinnanvaraisuus,
                      selectedHarkinnanvaraisuus,
                      setSelectedHarkinnanvaraisuus,
                    )
                  }
                  label={t(`raportti.${harkinnanvaraisuus}`)}
                />
              </Box>
            );
          })}
        </Box>
      )}
    />
  );
};
