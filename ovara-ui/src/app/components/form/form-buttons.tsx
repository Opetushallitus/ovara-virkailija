import { useTranslations } from 'next-intl';
import { OphButton } from '@opetushallitus/oph-design-system';
import { Box, Stack } from '@mui/material';
import { useSearchParams } from '@/app/hooks/useSearchParams';

export const FormButtons = ({
  disabled,
  excelDownloadUrl,
}: {
  disabled: boolean;
  excelDownloadUrl: string;
}) => {
  const t = useTranslations();

  return (
    <Box sx={{ display: 'flex', justifyContent: 'end' }}>
      <Stack direction="row" spacing={2}>
        <TyhjennaHakuehdotButton t={t} />
        <MuodostaExcelButton
          t={t}
          disabled={disabled}
          excelDownloadUrl={excelDownloadUrl}
        />
      </Stack>
    </Box>
  );
};

export const TyhjennaHakuehdotButton = ({
  t,
}: {
  t: typeof useTranslations;
}) => {
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
  t,
  disabled,
  excelDownloadUrl,
}: {
  t: typeof useTranslations;
  disabled: boolean;
  excelDownloadUrl: string;
}) => {
  return (
    <OphButton
      variant="contained"
      type="submit"
      disabled={disabled}
      href={excelDownloadUrl}
      download
    >
      {t('raportti.muodosta-raportti')}
    </OphButton>
  );
};
