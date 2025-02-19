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

  const hasToinenAsteAndKKRights = hasToinenAsteRights && hasKkRights;
  return (
    <MainContainer>
      {hasOvaraUserRights ? (
        <ListTable
          list={
            hasToinenAsteAndKKRights
              ? [...TOISEN_ASTEEN_RAPORTIT, ...KK_RAPORTIT]
              : hasToinenAsteRights
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
