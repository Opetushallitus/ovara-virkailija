'use client';

import { MainContainer } from '../components/main-container';
import { ListTable } from '../components/table/table';
import {
  KK_RAPORTIT,
  TIEDONSIIRTO_RAPORTIT,
  TIEDONSIIRTO_KK_RAPORTIT,
  TOISEN_ASTEEN_RAPORTIT,
} from '@/app/lib/constants';
import { useAuthorizedUser } from '../components/providers/authorized-user-provider';
import {
  hasOvaraRole,
  hasOvaraToinenAsteRole,
  hasOvaraKkRole,
  hasOvaraHakeneetRole,
  hasOvaraKkHakeneetRole,
} from '../lib/utils';
import { FullSpinner } from '@/app/components/full-spinner';

export default function Home() {
  const user = useAuthorizedUser();

  if (!user) {
    return <FullSpinner />;
  }
  const userRoles = user?.authorities;
  const hasOvaraUserRights = hasOvaraRole(userRoles);
  const hasToinenAsteRights = hasOvaraToinenAsteRole(userRoles);
  const hasKkRights = hasOvaraKkRole(userRoles);
  const hasHakeneetRights = hasOvaraHakeneetRole(userRoles);
  const hasKkHakeneetRights = hasOvaraKkHakeneetRole(userRoles);

  const raportit: Array<string> = [];
  if (hasToinenAsteRights) raportit.push(...TOISEN_ASTEEN_RAPORTIT);
  if (hasKkRights) raportit.push(...KK_RAPORTIT);
  if (hasHakeneetRights) raportit.push(...TIEDONSIIRTO_RAPORTIT);
  if (hasKkHakeneetRights) raportit.push(...TIEDONSIIRTO_KK_RAPORTIT);

  return (
    <MainContainer>
      {hasOvaraUserRights ? <ListTable list={raportit} /> : null}
    </MainContainer>
  );
}
