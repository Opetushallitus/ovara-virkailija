import { useTranslate } from '@tolgee/react';
import { OphButton } from '@opetushallitus/oph-design-system';
import { Box, Stack } from '@mui/material';
import { useSearchParams } from '@/app/hooks/searchParams/useSearchParams';

type ExcelDownloadButton = {
  disabled: boolean;
  downloadExcel: () => void;
};

export const FormButtons = ({
  disabled,
  downloadExcel,
}: ExcelDownloadButton) => {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'end' }}>
      <Stack direction="row" spacing={2}>
        <TyhjennaHakuehdotButton />
        <MuodostaExcelButton
          disabled={disabled}
          downloadExcel={downloadExcel}
        />
      </Stack>
    </Box>
  );
};

export const TyhjennaHakuehdotButton = () => {
  const { t } = useTranslate();
  const {
    setSelectedAlkamiskaudet,
    setSelectedHaut,
    setSelectedKoulutuksenTila,
    setSelectedToteutuksenTila,
    setSelectedHakukohteenTila,
    setSelectedValintakoe,
  } = useSearchParams();

  const emptySearchParams = () => {
    setSelectedAlkamiskaudet(null);
    setSelectedHaut(null);
    setSelectedKoulutuksenTila(null);
    setSelectedToteutuksenTila(null);
    setSelectedHakukohteenTila(null);
    setSelectedValintakoe(null);
  };

  return (
    <OphButton variant="outlined" onClick={emptySearchParams}>
      {t('raportti.tyhjenna-hakuehdot')}
    </OphButton>
  );
};

export const MuodostaExcelButton = ({
  disabled,
  downloadExcel,
}: ExcelDownloadButton) => {
  const { t } = useTranslate();

  return (
    <OphButton
      variant="contained"
      type="button"
      disabled={disabled}
      onClick={downloadExcel}
    >
      {t('raportti.muodosta-raportti')}
    </OphButton>
  );
};
