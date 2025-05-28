'use client';

import { Tolgee, DevTools } from '@tolgee/web';
import { TolgeeProvider, BackendFetch, FormatSimple } from '@tolgee/react';
import { ReactNode, useMemo } from 'react';
import Loading from '@/app/(root)/loading';
import type { LanguageCode } from '@/app/lib/types/common';
import { getConfiguration } from '@/app/lib/configuration/client-configuration';

const REVALIDATE_TIME_SECONDS = 60 * 60 * 2;

export function OvaraTolgeeProvider({
  language,
  children,
}: {
  language: LanguageCode;
  children: ReactNode;
}) {
  const config = getConfiguration();
  const tolgee = useMemo(
    () =>
      Tolgee()
        .use(DevTools()) // Optional
        .use(FormatSimple())
        .use(
          BackendFetch({
            prefix: config.lokalisointiPrefix,
            next: {
              revalidate: REVALIDATE_TIME_SECONDS,
            },
          }),
        )
        .init({
          language,
          availableLanguages: ['fi', 'sv', 'en'],
          defaultLanguage: 'fi',
          defaultNs: 'ovara',
          ns: ['ovara'],
          projectId: 11100,
        }),
    [config.lokalisointiPrefix, language],
  );

  return (
    <TolgeeProvider tolgee={tolgee} fallback={<Loading />}>
      {children}
    </TolgeeProvider>
  );
}
