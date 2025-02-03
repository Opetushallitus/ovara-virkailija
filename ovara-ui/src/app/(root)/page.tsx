'use client';

import { MainContainer } from '../components/main-container';
import { ListTable } from '../components/table/table';
import { OPON_RAPORTIT, TOISEN_ASTEEN_RAPORTIT } from '@/app/lib/constants';
import { useAuthorizedUser } from '../contexts/AuthorizedUserProvider';
import {
  hasOvaraOpoRole,
  hasOvaraRole,
  hasOvaraToinenAsteRole,
} from '../lib/utils';

export default function Home() {
  const user = useAuthorizedUser();

  const userRoles = user?.authorities;
  const hasOvaraUserRights = hasOvaraRole(userRoles);
  const hasToinenAsteRights = hasOvaraToinenAsteRole(userRoles);
  const hasOpoRights = hasOvaraOpoRole(userRoles);

  const reportList = [
    ...(hasToinenAsteRights ? TOISEN_ASTEEN_RAPORTIT : []),
    ...(hasOpoRights ? OPON_RAPORTIT : []),
  ];

  return (
    <MainContainer>
      {user ? <p>Welcome, {user.userOid}!</p> : null}
      {hasOvaraUserRights ? <ListTable list={reportList} /> : null}
    </MainContainer>
  );
}
