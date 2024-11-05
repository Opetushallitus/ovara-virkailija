import { useTranslations } from 'next-intl';
import { OphButton } from '@opetushallitus/oph-design-system';
import { Box, Stack } from '@mui/material';
import { useSearchParams } from '@/app/hooks/useSearchParams';

export const FormButtons = ({ disabled }: { disabled: boolean }) => {
  const t = useTranslations();

  return (
    <Box sx={{ display: 'flex', justifyContent: 'end' }}>
      <Stack direction="row" spacing={2}>
        <TyhjennaHakuehdotButton t={t} />
        <MuodostaExcelButton t={t} disabled={disabled} />
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
}: {
  t: typeof useTranslations;
  disabled: boolean;
}) => {
  return (
    <OphButton variant="contained" type="submit" disabled={disabled}>
      {t('raportti.muodosta-raportti')}
    </OphButton>
  );
};
