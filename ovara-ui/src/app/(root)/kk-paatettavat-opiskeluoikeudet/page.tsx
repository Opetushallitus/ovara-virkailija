import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';
import { hasOvaraKkYosRole } from '@/app/lib/utils';
import { useFetchOrganisaatiohierarkiat } from '@/app/hooks/useFetchOrganisaatiohierarkiat';
import { isEmpty } from 'remeda';
import { useDownloadWithErrorBoundary } from '@/app/hooks/useDownloadWithErrorBoundary';
import { useOptimisticSearchParams } from 'nuqs/adapters/react-router/v7';
import { downloadExcel } from '@/app/components/form/utils';
import { MainContainer } from '@/app/components/main-container';
import { FormBox } from '@/app/components/form/form-box';
import { SpinnerModal } from '@/app/components/form/spinner-modal';
import { OphTypography } from '@opetushallitus/oph-design-system';
import { DebouncedOphInput } from '@/app/components/form/DebouncedOphInput';
import { Box, Divider } from '@mui/material';
import { OppilaitosSelect } from '@/app/components/form/organisaatiovalikot';
import { FormButtons } from '@/app/components/form/form-buttons';
import { OpiskeluoikeudenTila } from '@/app/components/form/opiskeluoikeuden-tila';
import { OvaraTextInput } from '@/app/components/form/OvaraTextInput';
import { usePaatettavatOpiskeluoikeudetSearchParams } from '@/app/hooks/searchParams/usePaatettavatOpiskeluoikeudetSearchParams';
import { OvaraFormControl } from '@/app/components/form/ovara-form-control';

const LabelSpacer = () => <div style={{ width: '96px' }} />;

export default function KkPaatettavatOpiskeluoikeudet() {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const hasKkYosRights = hasOvaraKkYosRole(user?.authorities);
  const {
    etunimi,
    setEtunimi,
    sukunimi,
    setSukunimi,
    hetu,
    setHetu,
    oppijanumero,
    setOppijanumero,
    selectedOppilaitos,
  } = usePaatettavatOpiskeluoikeudetSearchParams();
  const { data: organisaatiot } = useFetchOrganisaatiohierarkiat();

  const isDisabled = [selectedOppilaitos || []].every(isEmpty);
  const { run, isLoading } = useDownloadWithErrorBoundary();

  const queryParams = useOptimisticSearchParams();
  const queryParamsStr = queryParams.toString();

  const handleDownload = () =>
    run(() =>
      downloadExcel('kk-paatettavat-opiskeluoikeudet/excel', queryParamsStr),
    );

  return (
    <MainContainer>
      {hasKkYosRights ? (
        <FormBox>
          {isLoading && <SpinnerModal open />}
          <OphTypography>{t('yleinen.pakolliset-kentat')}</OphTypography>
          <OppilaitosSelect organisaatiot={organisaatiot} required={true} />
          <Divider />
          <Box sx={{ display: 'flex', width: '100%', gap: 2 }}>
            <LabelSpacer />
            <OvaraFormControl
              sx={{ flex: 1, minWidth: 0, pb: 2 }}
              label={t('raportti.sukunimi')}
              renderInput={({ labelId }) => (
                <DebouncedOphInput
                  fullWidth
                  value={sukunimi}
                  onValueChange={setSukunimi}
                  inputProps={{
                    'aria-labelledby': labelId,
                  }}
                />
              )}
            />
            <OvaraFormControl
              sx={{
                flex: 1,
                minWidth: 0,
                pb: 2,
                '& .MuiFormLabel-root': {
                  width: 'auto',
                  paddingRight: '0.5rem',
                  whiteSpace: 'nowrap',
                },
              }}
              label={t('raportti.etunimet')}
              renderInput={({ labelId }) => (
                <DebouncedOphInput
                  fullWidth
                  value={etunimi}
                  onValueChange={setEtunimi}
                  inputProps={{
                    'aria-labelledby': labelId,
                  }}
                />
              )}
            />
          </Box>
          <OvaraTextInput
            label={t('raportti.hetu')}
            value={hetu}
            onValueChange={setHetu}
          />
          <OvaraTextInput
            label={t('raportti.oppijanumero')}
            value={oppijanumero}
            onValueChange={setOppijanumero}
          />
          <Divider />
          <OpiskeluoikeudenTila />
          <FormButtons disabled={isDisabled} downloadExcel={handleDownload} />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
