'use client';

import { Tolgee, DevTools } from '@tolgee/web';
import { TolgeeProvider, BackendFetch, FormatSimple } from '@tolgee/react';
import { configuration } from '@/app/lib/configuration';
import { ReactNode, useMemo } from 'react';
import Loading from '@/app/(root)/loading';
import type { LanguageCode } from '@/app/lib/types/common';

const REVALIDATE_TIME_SECONDS = 60 * 60 * 2;

export function OvaraTolgeeProvider({
  language,
  children,
}: {
  language: LanguageCode;
  children: ReactNode;
}) {
  const tolgee = useMemo(
    () =>
      Tolgee()
        .use(DevTools()) // Optional
        .use(FormatSimple())
        .use(
          BackendFetch({
            prefix: configuration.lokalisointiPrefix,
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
    [language],
  );

  return (
    <TolgeeProvider tolgee={tolgee} fallback={<Loading />}>
      {children}
    </TolgeeProvider>
  );
}
