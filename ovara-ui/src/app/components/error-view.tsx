'use client';

import { FetchError, PermissionError } from '@/app/lib/common';
import { useTranslate } from '@tolgee/react';
import { Stack } from '@mui/material';
import { OphButton, OphTypography } from '@opetushallitus/oph-design-system';
import { ReactNode } from 'react';

type ErrorComponentProps = {
  title?: string;
  message?: ReactNode;
  retry?: () => void;
};

const ErrorComponent = ({ title, message, retry }: ErrorComponentProps) => {
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

type ErrorBody = string | string[] | { details: string[] } | null;

const renderErrorBody = (body: ErrorBody): ReactNode => {
  if (!body) return null;

  if (Array.isArray(body)) {
    return (
      <Stack spacing={1}>
        {body.map((msg, idx) => (
          <OphTypography key={idx} sx={{ overflowWrap: 'anywhere' }}>
            {msg}
          </OphTypography>
        ))}
      </Stack>
    );
  }

  if (typeof body === 'string') {
    return (
      <OphTypography sx={{ overflowWrap: 'anywhere' }}>{body}</OphTypography>
    );
  }

  if ('details' in body && Array.isArray(body.details)) {
    return (
      <Stack spacing={1}>
        {body.details.map((msg, idx) => (
          <OphTypography key={idx} sx={{ overflowWrap: 'anywhere' }}>
            {msg}
          </OphTypography>
        ))}
      </Stack>
    );
  }

  return null;
};

export function ErrorView({
  error,
  reset,
}: {
  error: (Error & { digest?: string }) | FetchError;
  reset: () => void;
}) {
  const { t } = useTranslate();

  if (error instanceof FetchError) {
    return (
      <ErrorComponent
        title={t('virhe.palvelin')}
        message={
          <Stack spacing={1}>
            <OphTypography sx={{ overflowWrap: 'anywhere' }}>
              URL: {error.response.url}
            </OphTypography>
            <OphTypography>
              {t('virhe.virhekoodi')} {error.response.status}
            </OphTypography>
            {renderErrorBody(error.body as ErrorBody)}
          </Stack>
        }
        retry={reset}
      />
    );
  }

  if (error instanceof PermissionError) {
    return <ErrorComponent message={error.message} />;
  }

  return <ErrorComponent title={t('virhe.tuntematon')} retry={reset} />;
}
