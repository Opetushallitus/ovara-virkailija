'use client';
import { useContext, createContext } from 'react';
import { useFetchUser } from '@/app/hooks/useFetchUser';

const AuthorizedUserContext = createContext();

export function AuthorizedUserProvider({ children }) {
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
