'use client';

import { MainContainer } from '../components/main-container';
import { ListTable } from '../components/table/table';
import { useAuthorizedUser } from '../components/providers/authorized-user-provider';
import { hasOvaraRole, getRaporttiListByUserRights } from '../lib/utils';
import { FullSpinner } from '@/app/components/full-spinner';

export default function Home() {
  const user = useAuthorizedUser();

  if (!user) {
    return <FullSpinner />;
  }
  const userRoles = user?.authorities;
  const hasOvaraUserRights = hasOvaraRole(userRoles);

  return (
    <MainContainer>
      {hasOvaraUserRights ? (
        <ListTable list={getRaporttiListByUserRights(userRoles)} />
      ) : null}
    </MainContainer>
  );
}
