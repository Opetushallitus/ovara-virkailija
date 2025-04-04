import { useTranslate } from '@tolgee/react';
import { OphButton } from '@opetushallitus/oph-design-system';
import { Box, Stack } from '@mui/material';
import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';

type ExcelDownloadButton = {
  disabled: boolean;
  downloadExcel: () => void;
};

export const FormButtons = ({
  disabled,
  downloadExcel,
  fieldsToClear,
}: {
  disabled: boolean;
  downloadExcel: () => void;
  fieldsToClear?: Array<() => void>;
}) => {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'end', paddingTop: '1rem' }}>
      <Stack direction="row" spacing={2}>
        <TyhjennaHakuehdotButton fieldsToClear={fieldsToClear} />
        <MuodostaExcelButton
          disabled={disabled}
          downloadExcel={downloadExcel}
        />
      </Stack>
    </Box>
  );
};

export const TyhjennaHakuehdotButton = ({
  fieldsToClear,
}: {
  fieldsToClear?: Array<() => void>;
}) => {
  const { t } = useTranslate();

  const emptySearchParams = () => {
    fieldsToClear?.forEach((clearField) => clearField());
  };

  const { emptyAllHakijatParams } = useHakijatSearchParams();
  const { emptyAllCommonParams } = useCommonSearchParams();
  const { emptyAllHakeneetParams } = useHakeneetSearchParams();

  const emptyAllSearchParams = () => {
    emptySearchParams();
    emptyAllHakijatParams();
    emptyAllHakeneetParams();
    emptyAllCommonParams();
  };

  return (
    <OphButton variant="outlined" onClick={() => emptyAllSearchParams()}>
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
