'use client';

import { MainContainer } from '../components/main-container';
import { ListTable } from '../components/table/table';
import { TOISEN_ASTEEN_RAPORTIT } from '@/app/lib/constants';
import { useFetchUser } from '@/app/hooks/useFetchUser';

export default function Home() {
  const user = useFetchUser();

  const userRoles = user?.authorities;
  const hasRaportointiRole = userRoles?.includes('ROLE_APP_RAPORTOINTI');
  return (
    <MainContainer>
      {user ? <p>Welcome, {user.userOid}!</p> : null}
      {hasRaportointiRole ? <ListTable list={TOISEN_ASTEEN_RAPORTIT} /> : null}
    </MainContainer>
  );
}
