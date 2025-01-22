import { useTranslate } from '@tolgee/react';
import { OphButton } from '@opetushallitus/oph-design-system';
import { Box, Stack } from '@mui/material';
import { useSearchParams } from '@/app/hooks/searchParams/useSearchParams';

export const FormButtons = ({
  disabled,
  excelDownloadUrl,
}: {
  disabled: boolean;
  excelDownloadUrl: string;
}) => {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'end' }}>
      <Stack direction="row" spacing={2}>
        <TyhjennaHakuehdotButton />
        <MuodostaExcelButton
          disabled={disabled}
          excelDownloadUrl={excelDownloadUrl}
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
  excelDownloadUrl,
}: {
  disabled: boolean;
  excelDownloadUrl: string;
}) => {
  const { t } = useTranslate();
  return (
    <OphButton
      variant="contained"
      type="submit"
      disabled={disabled}
      href={excelDownloadUrl}
    >
      {t('raportti.muodosta-raportti')}
    </OphButton>
  );
};
