'use client';

import { MainContainer } from '../components/main-container';
import { ListTable } from '../components/table/table';
import { TOISEN_ASTEEN_RAPORTIT } from '@/app/lib/constants';
import { useFetchUser } from '@/app/hooks/useFetchUser';

type RaporttiList = {
  raportit: Array<string>;
};

const RaporttiLinksList = ({ raportit }: RaporttiList) => {
  return <ListTable list={raportit} />;
};

export default function Home() {
  const user = useFetchUser();

  return (
    <MainContainer>
      {user ? <p>Welcome, {user.userOid}!</p> : null}
      <RaporttiLinksList raportit={TOISEN_ASTEEN_RAPORTIT} />
    </MainContainer>
  );
}
