import { useSearchParams } from '@/app/hooks/searchParams/useSearchParams';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { Box } from '@mui/material';
import { OphCheckbox } from '@opetushallitus/oph-design-system';
import { OvaraFormControl } from '@/app/components/form/ovara-form-control';
import { isChecked, changeChecked } from '@/app/components/form/utils';
import { getHarkinnanvaraisuusTranslationKey } from '@/app/lib/utils';

export const Harkinnanvaraisuus = ({ t }: { t: (key: string) => string }) => {
  const { data: harkinnanvaraisuudet } = useQuery({
    queryKey: ['fetchHarkinnanvaraisuudet'],
    queryFn: () => doApiFetch('harkinnanvaraisuudet'),
  });

  const { selectedHarkinnanvaraisuus, setSelectedHarkinnanvaraisuus } =
    useSearchParams();

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
            width: '100%',
          }}
        >
          {harkinnanvaraisuudet?.map((harkinnanvaraisuus: string) => {
            return (
              <Box key={harkinnanvaraisuus}>
                <OphCheckbox
                  id={harkinnanvaraisuus}
                  checked={isChecked(
                    harkinnanvaraisuus,
                    selectedHarkinnanvaraisuus,
                  )}
                  onChange={(e) =>
                    changeChecked(
                      e,
                      harkinnanvaraisuus,
                      selectedHarkinnanvaraisuus,
                      setSelectedHarkinnanvaraisuus,
                    )
                  }
                  label={t(
                    `raportti.${getHarkinnanvaraisuusTranslationKey(harkinnanvaraisuus)}`,
                  )}
                />
              </Box>
            );
          })}
        </Box>
      )}
    />
  );
};
