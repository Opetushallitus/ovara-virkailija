'use client';
import { useContext, createContext, ReactNode } from 'react';
import { useFetchUser } from '@/app/hooks/useFetchUser';

type User = {
  userOid: string;
  authorities: Array<string>;
};

const AuthorizedUserContext = createContext<User | null>(null);

export function AuthorizedUserProvider({ children }: { children: ReactNode }) {
  const user = useFetchUser();

  return (
    <AuthorizedUserContext.Provider value={user}>
      {children}
    </AuthorizedUserContext.Provider>
  );
}

export function useAuthorizedUser() {
  return useContext(AuthorizedUserContext);
}
