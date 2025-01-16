import { forwardRef } from 'react';
import { useTranslate } from '@tolgee/react';
import { OphButton } from '@opetushallitus/oph-design-system';
import { Box, Stack, type ButtonProps } from '@mui/material';
import { useSearchParams } from '@/app/hooks/useSearchParams';

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

export type DownloadButtonProps = Omit<ButtonProps, 'endIcon'> & {
  download: boolean;
};

export const DownloadButton = forwardRef<
  HTMLButtonElement,
  DownloadButtonProps
>(function renderButton(props, ref) {
  return <OphButton {...props} ref={ref} />;
});

export const MuodostaExcelButton = ({
  disabled,
  excelDownloadUrl,
}: {
  disabled: boolean;
  excelDownloadUrl: string;
}) => {
  const { t } = useTranslate();
  return (
    <DownloadButton
      variant="contained"
      type="submit"
      disabled={disabled}
      href={excelDownloadUrl}
      download
    >
      {t('raportti.muodosta-raportti')}
    </DownloadButton>
  );
};
