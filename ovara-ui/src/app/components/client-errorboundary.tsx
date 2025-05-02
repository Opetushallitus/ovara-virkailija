'use client';

import { ReactNode } from 'react';
import { Stack } from '@mui/material';
import { useTranslate } from '@tolgee/react';
import { OphButton, OphTypography } from '@opetushallitus/oph-design-system';
import { ErrorBoundary } from 'react-error-boundary';

const ErrorComponent = ({
  title,
  message,
  retry,
}: {
  title?: string;
  message?: ReactNode;
  retry?: () => void;
}) => {
  const { t } = useTranslate();
  return (
    <Stack spacing={1} sx={{ margin: 1 }} alignItems="flex-start">
      {title && <OphTypography variant="h1">{title}</OphTypography>}
      {message && <OphTypography component="div">{message}</OphTypography>}
      {retry && (
        <OphButton variant="contained" onClick={retry}>
          {t('virhe.uudelleenyritys')}
        </OphButton>
      )}
    </Stack>
  );
};

export function ClientErrorBoundary({ children }: { children: ReactNode }) {
  const { t } = useTranslate();

  return (
    <ErrorBoundary
      fallbackRender={({ error, resetErrorBoundary }) => (
        <ErrorComponent
          title={t('virhe.sovellusvirhe') /* <-- now using t function */}
          message={error.message}
          retry={resetErrorBoundary}
        />
      )}
      onError={(error, info) => {
        console.error('Caught by ClientErrorBoundary:', error, info);
      }}
    >
      {children}
    </ErrorBoundary>
  );
}
