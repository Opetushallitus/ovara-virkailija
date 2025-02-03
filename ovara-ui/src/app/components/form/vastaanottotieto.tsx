import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { Box, SelectChangeEvent } from '@mui/material';
import { OphCheckbox } from '@opetushallitus/oph-design-system';
import { OvaraFormControl } from '@/app/components/form/ovara-form-control';
import { isEmpty, isNullish } from 'remeda';

export const Vastaanottotieto = ({ t }: { t: (key: string) => string }) => {
  const { selectedVastaanottotieto, setSelectedVastaanottotieto } =
    useHakijatSearchParams();

  const includesVastaanottaneet = isNullish(selectedVastaanottotieto)
    ? false
    : selectedVastaanottotieto?.includes('vastaanottaneet');

  const includesPeruneet = isNullish(selectedVastaanottotieto)
    ? false
    : selectedVastaanottotieto?.includes('peruneet');

  const includesPeruuntunut = isNullish(selectedVastaanottotieto)
    ? false
    : selectedVastaanottotieto?.includes('peruuntunut');

  const changeVastaanottaneetSelection = (
    e: SelectChangeEvent,
    includesValue: boolean,
  ) => {
    const id = e.target.id;
    let newValue = null;

    if (includesValue) {
      newValue = selectedVastaanottotieto.filter((value) => id !== value);
    } else {
      if (isNullish(selectedVastaanottotieto)) {
        newValue = [id];
      } else {
        newValue = selectedVastaanottotieto?.concat([id]);
      }
    }

    setSelectedVastaanottotieto(isEmpty(newValue) ? null : newValue);
  };

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
          <OphCheckbox
            id={'vastaanottaneet'}
            checked={includesVastaanottaneet}
            onChange={(e) =>
              changeVastaanottaneetSelection(e, includesVastaanottaneet)
            }
            label={t(`raportti.vastaanottaneet`)}
          />
          <OphCheckbox
            id={'peruneet'}
            checked={includesPeruneet}
            onChange={(e) =>
              changeVastaanottaneetSelection(e, includesPeruneet)
            }
            label={t('raportti.peruneet')}
          />
          <OphCheckbox
            id={'peruuntunut'}
            checked={includesPeruuntunut}
            onChange={(e) =>
              changeVastaanottaneetSelection(e, includesPeruuntunut)
            }
            label={t('raportti.peruuntunut')}
          />
        </Box>
      )}
    />
  );
};
