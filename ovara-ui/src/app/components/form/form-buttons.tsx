import { useTranslate } from '@tolgee/react';
import { OphButton } from '@opetushallitus/oph-design-system';
import { Box, Stack } from '@mui/material';

export const FormButtons = ({
  disabled,
  excelDownloadUrl,
  fieldsToClear,
}: {
  disabled: boolean;
  excelDownloadUrl: string;
  fieldsToClear: Array<() => void>;
}) => {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'end' }}>
      <Stack direction="row" spacing={2}>
        <TyhjennaHakuehdotButton fieldsToClear={fieldsToClear} />
        <MuodostaExcelButton
          disabled={disabled}
          excelDownloadUrl={excelDownloadUrl}
        />
      </Stack>
    </Box>
  );
};

export const TyhjennaHakuehdotButton = ({
  fieldsToClear,
}: {
  fieldsToClear: Array<() => void>;
}) => {
  const { t } = useTranslate();

  const emptySearchParams = () => {
    fieldsToClear.forEach((clearField) => clearField());
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
