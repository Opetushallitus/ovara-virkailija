'use client';

import { createContext, useContext, useMemo } from 'react';
import type { LanguageCode } from '@/app/lib/types/common';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';

const LanguageContext = createContext<LanguageCode>('fi');

export const LanguageProvider = ({
  children,
}: {
  children: React.ReactNode;
}) => {
  const user = useAuthorizedUser();

  const language: LanguageCode = useMemo(() => {
    const lang = user?.asiointikieli;
    return lang && ['fi', 'sv', 'en'].includes(lang)
      ? (lang as LanguageCode)
      : 'fi';
  }, [user]);

  return (
    <LanguageContext.Provider value={language}>
      {children}
    </LanguageContext.Provider>
  );
};

export const useLanguage = () => useContext(LanguageContext);
