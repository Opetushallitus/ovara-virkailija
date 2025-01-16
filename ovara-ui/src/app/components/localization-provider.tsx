'use client';
import { useEffect } from 'react';
import { configuration } from '@/app/lib/configuration';
import { Tolgee } from '@tolgee/web';
import { BackendFetch, FormatSimple, TolgeeProvider } from '@tolgee/react';
import Loading from '@/app/(root)/loading';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';

const REVALIDATE_TIME_SECONDS = 60 * 60 * 2;

const LocalizationContent = ({
  language,
  children,
}: {
  language: string;
  children: React.ReactNode;
}) => {
  const tolgee = Tolgee()
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
      language: language,
      availableLanguages: ['fi', 'sv', 'en'],
      defaultNs: 'ovara',
      ns: ['ovara'],
      projectId: 11100,
    });

  return (
    <TolgeeProvider tolgee={tolgee} fallback={<Loading />}>
      {children}
    </TolgeeProvider>
  );
};

export default function LocalizationProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  const user = useAuthorizedUser();
  const language = user?.asiointikieli;

  useEffect(() => {
    if (language) {
      document.documentElement.setAttribute('lang', language);
    }
  }, [language]);

  if (!language) {
    return <Loading />;
  }

  return (
    <LocalizationContent language={language}>{children}</LocalizationContent>
  );
}
