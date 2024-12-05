import { useEffect } from 'react';
import { FetchError, PermissionError } from '../lib/common';
import { useTranslations } from 'next-intl';

export function ErrorView({
  error,
  reset,
}: {
  error: (Error & { digest?: string }) | FetchError;
  reset: () => void;
}) {
  useEffect(() => {
    console.error(error);
  });

  const t = useTranslations('error');

  if (error instanceof FetchError) {
    return (
      <>
        <h1>{t('palvelin')}</h1>
        <p>
          {t('virhekoodi')} {error.response.status}
        </p>
        <button onClick={() => reset()}>{t('uudelleenyritys')}</button>
      </>
    );
  } else if (error instanceof PermissionError) {
    return <p>{error.message}</p>;
  } else {
    return (
      <>
        <h1>{t('tuntematon')}</h1>
        <button onClick={() => reset()}>{t('uudelleenyritys')}</button>
      </>
    );
  }
}
