import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { Box } from '@mui/material';
import { OphCheckbox } from '@opetushallitus/oph-design-system';
import { OvaraFormControl } from '@/app/components/form/ovara-form-control';
import { changeChecked, isChecked } from './utils';

export const Vastaanottotieto = ({ t }: { t: (key: string) => string }) => {
  const { selectedVastaanottotieto, setSelectedVastaanottotieto } =
    useHakijatSearchParams();

  const vastaanottoSelection = ['vastaanottaneet', 'peruneet', 'peruuntunut'];

  return (
    <OvaraFormControl
      sx={{
        marginBottom: '20px',
      }}
      label={t(`raportti.vastaanottotieto`)}
      renderInput={() => (
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'row',
            columnGap: 2,
            width: '100%',
          }}
        >
          {vastaanottoSelection.map((vastaanottotieto) => {
            return (
              <OphCheckbox
                key={vastaanottotieto}
                id={vastaanottotieto}
                checked={isChecked(vastaanottotieto, selectedVastaanottotieto)}
                onChange={(e) =>
                  changeChecked(
                    e,
                    vastaanottotieto,
                    selectedVastaanottotieto,
                    setSelectedVastaanottotieto,
                  )
                }
                label={t(`raportti.${vastaanottotieto}`)}
              />
            );
          })}
        </Box>
      )}
    />
  );
};
