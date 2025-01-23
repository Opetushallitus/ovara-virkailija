'use client';

import { MainContainer } from '../components/main-container';
import { ListTable } from '../components/table/table';
import { TOISEN_ASTEEN_RAPORTIT } from '@/app/lib/constants';
import { useAuthorizedUser } from '../contexts/AuthorizedUserProvider';
import { hasOvaraRole, hasOvaraToinenAsteRole } from '../lib/utils';

export default function Home() {
  const user = useAuthorizedUser();

  const userRoles = user?.authorities;
  const hasOvaraUserRights = hasOvaraRole(userRoles);
  const hasToinenAsteRights = hasOvaraToinenAsteRole(userRoles);
  return (
    <MainContainer>
      {user ? <p>Welcome, {user.userOid}!</p> : null}
      {hasOvaraUserRights ? (
        <ListTable list={hasToinenAsteRights ? TOISEN_ASTEEN_RAPORTIT : []} />
      ) : null}
    </MainContainer>
  );
}
