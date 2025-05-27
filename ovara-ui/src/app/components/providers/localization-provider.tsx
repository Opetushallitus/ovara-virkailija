'use client';

import { ReactNode, useEffect } from 'react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import type { LanguageCode } from '@/app/lib/types/common';
import { OvaraTolgeeProvider } from '@/app/components/providers/ovara-tolgee-provider';

export default function LocalizationProvider({
  children,
}: {
  children: ReactNode;
}) {
  const user = useAuthorizedUser();
  const language = (user?.asiointikieli ?? 'fi') as LanguageCode;

  useEffect(() => {
    document.documentElement.setAttribute('lang', language);
  }, [language]);

  return (
    <OvaraTolgeeProvider language={language}>{children}</OvaraTolgeeProvider>
  );
}
