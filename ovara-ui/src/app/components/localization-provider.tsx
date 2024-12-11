'use client';
import { NextIntlClientProvider } from 'next-intl';
import { useFetchUser } from '../hooks/useFetchUser';
import { configuration } from '@/app/lib/configuration';
import { useFetchTranslations } from '../hooks/useFetchLokalisaatiot';

const REVALIDATE_TIME_SECONDS = 60 * 60 * 2;

export async function fetchLokalisaatiot(lang: string) {
  const url = `${configuration.virkailijaUrl}/lokalisointi/cxf/rest/v1/localisation?category=viestinvalitys&locale=`;
  const res = await fetch(`${url}${lang}`, {
    next: { revalidate: REVALIDATE_TIME_SECONDS },
  });

  return res.json();
}

const LocalizationContent = ({
  lng,
  messagesFromLocalFile,
  children,
}: {
  messagesFromLocalFile: IntlMessages;
  children: React.ReactNode;
  lng?: string;
}) => {
  const locale = lng ?? 'fi';
  const messagesFromLokalisointi = useFetchTranslations(locale) as IntlMessages;
  const messages = process.env.DEV
    ? messagesFromLocalFile
    : messagesFromLokalisointi;

  const timeZone = 'Europe/Helsinki';

  return (
    <NextIntlClientProvider
      locale={locale}
      messages={messages}
      timeZone={timeZone}
    >
      {children}
    </NextIntlClientProvider>
  );
};

export default function LocalizationProvider({
  messagesFromLocalFile,
  children,
}: {
  messagesFromLocalFile: IntlMessages;
  children: React.ReactNode;
}) {
  const user = useFetchUser();
  const language = user?.asiointikieli;

  return (
    <LocalizationContent
      lng={language}
      messagesFromLocalFile={messagesFromLocalFile}
    >
      {children}
    </LocalizationContent>
  );
}
