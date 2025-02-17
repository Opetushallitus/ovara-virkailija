'use client';

import { MainContainer } from '../components/main-container';
import { ListTable } from '../components/table/table';
import { KK_RAPORTIT, TOISEN_ASTEEN_RAPORTIT } from '@/app/lib/constants';
import { useAuthorizedUser } from '../contexts/AuthorizedUserProvider';
import {
  hasOvaraRole,
  hasOvaraToinenAsteRole,
  hasOvaraKkRole,
} from '../lib/utils';

export default function Home() {
  const user = useAuthorizedUser();

  const userRoles = user?.authorities;
  const hasOvaraUserRights = hasOvaraRole(userRoles);
  const hasToinenAsteRights = hasOvaraToinenAsteRole(userRoles);
  const hasKkRights = hasOvaraKkRole(userRoles);
  return (
    <MainContainer>
      {hasOvaraUserRights ? (
        <ListTable
          list={
            hasToinenAsteRights
              ? TOISEN_ASTEEN_RAPORTIT
              : hasKkRights
                ? KK_RAPORTIT
                : []
          }
        />
      ) : null}
    </MainContainer>
  );
}
