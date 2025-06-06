'use client';
import React, { useContext, createContext, ReactNode } from 'react';
import { useFetchUser } from '@/app/hooks/useFetchUser';
import { User } from '@/app/lib/types/common';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { FullSpinner } from '@/app/components/full-spinner';
import { PermissionError } from '@/app/lib/common';
import { hasOvaraRole } from '@/app/lib/utils';
import { isNullish } from 'remeda';

const AuthorizedUserContext = createContext<User | null>(null);

export function AuthorizedUserProvider({ children }: { children: ReactNode }) {
  const user = useFetchUser();

  const { isLoading } = useQuery({
    queryKey: ['session'],
    queryFn: async () => {
      // api hoitaa virheenkäsittelyn ja http 401 tilanteen uudelleenohjauksen
      const response = await doApiFetch('session', {}, 'no-store');
      return response; // jotain on pakko palauttaa querysta vaikka sitä ei käytetä
    },
    refetchInterval: 60000, // Pollataan session voimassaoloa 60 sekunnin välein
    staleTime: 0, // Ei cachea
    enabled: !isNullish(user),
    retry: false,
  });

  if (isLoading || isNullish(user)) {
    return <FullSpinner />;
  }

  const hasPermission = user && hasOvaraRole(user.authorities);

  if (!hasPermission) {
    throw new PermissionError();
  }
  return (
    <AuthorizedUserContext.Provider value={user}>
      {children}
    </AuthorizedUserContext.Provider>
  );
}

export function useAuthorizedUser() {
  return useContext(AuthorizedUserContext);
}
