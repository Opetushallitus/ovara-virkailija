'use client';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';
import { hasOvaraKkYosRole } from '@/app/lib/utils';
import { useFetchOrganisaatiohierarkiat } from '@/app/hooks/useFetchOrganisaatiohierarkiat';
import { isEmpty } from 'remeda';
import { useDownloadWithErrorBoundary } from '@/app/hooks/useDownloadWithErrorBoundary';
import { useSearchParams } from 'next/navigation';
import { downloadExcel } from '@/app/components/form/utils';
import { MainContainer } from '@/app/components/main-container';
import { FormBox } from '@/app/components/form/form-box';
import { SpinnerModal } from '@/app/components/form/spinner-modal';
import { OphInput, OphTypography } from '@opetushallitus/oph-design-system';
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
  const hasKkYosRights = hasOvaraYosKkRole(user?.authorities);
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

  const queryParamsStr = useSearchParams().toString();

  const handleDownload = () =>
    run(() => downloadExcel('kk-paatettavat-opiskeluoikeudet', queryParamsStr));

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
                <OphInput
                  fullWidth
                  value={sukunimi}
                  onChange={(e) => setSukunimi(e.target.value)}
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
                <OphInput
                  fullWidth
                  value={etunimi}
                  onChange={(e) => setEtunimi(e.target.value)}
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
            onChange={(e) => setHetu(e.target.value)}
          />
          <OvaraTextInput
            label={t('raportti.oppijanumero')}
            value={oppijanumero}
            onChange={(e) => setOppijanumero(e.target.value)}
          />
          <Divider />
          <OpiskeluoikeudenTila />
          <FormButtons disabled={isDisabled} downloadExcel={handleDownload} />
        </FormBox>
      ) : null}
    </MainContainer>
  );
}
