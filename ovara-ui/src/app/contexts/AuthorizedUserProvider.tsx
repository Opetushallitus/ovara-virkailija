'use client';
import React, { createContext, ReactNode, useContext } from 'react';
import { User } from '@/app/lib/types/common';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { FullSpinner } from '@/app/components/full-spinner';

const AuthorizedUserContext = createContext<User | null>(null);

export function AuthorizedUserProvider({ children }: { children: ReactNode }) {
  const { data: user, isLoading } = useQuery<User>({
    queryKey: ['user'],
    queryFn: async () => {
      const response = await doApiFetch('user', undefined, 'no-store');
      return response?.user ?? null;
    },
    staleTime: 0,
    retry: false,
  });

  // Pollataan session voimassaoloa 60 sekunnin välein
  const { isLoading: isSessionLoading } = useQuery({
    queryKey: ['session'],
    queryFn: async () => {
      const response = await doApiFetch('session', {}, 'no-store');
      return response; // React query vaatii palauttamaan jotain vaikka sitä ei tässä käytetä
    },
    refetchInterval: 60000, // 60s
    staleTime: 0, // ei cachea
    enabled: !!user, // Pollataan sessiota vasta kun käyttäjä on ladattu
    retry: false,
  });

  if (isLoading || isSessionLoading || !user) {
    return <FullSpinner />;
  }

  return (
    <AuthorizedUserContext.Provider value={user ?? null}>
      {children}
    </AuthorizedUserContext.Provider>
  );
}

export function useAuthorizedUser() {
  return useContext(AuthorizedUserContext);
}
