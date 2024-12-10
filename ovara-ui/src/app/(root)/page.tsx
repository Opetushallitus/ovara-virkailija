'use client';

import { MainContainer } from '../components/main-container';
import { ListTable } from '../components/table/table';
import { TOISEN_ASTEEN_RAPORTIT } from '@/app/lib/constants';
import { useAuthorizedUser } from '../contexts/AuthorizedUserProvider';

export default function Home() {
  const user = useAuthorizedUser();

  const userRoles = user?.authorities;
  const hasOvaraRole = userRoles?.includes('ROLE_APP_OVARA-VIRKAILIJA');
  return (
    <MainContainer>
      {user ? <p>Welcome, {user.userOid}!</p> : null}
      {hasOvaraRole ? <ListTable list={TOISEN_ASTEEN_RAPORTIT} /> : null}
    </MainContainer>
  );
}
