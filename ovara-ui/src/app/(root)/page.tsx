'use client';

import { useState } from 'react';

import { OphButton, OphTypography } from '@opetushallitus/oph-design-system';
import { getPing } from '../lib/ovara-backend';
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
  const [message, setMessage] = useState('testi');

  const onGetPing = async () => {
    try {
      const res = await getPing();
      setMessage(res.data as string);
    } catch (e) {
      setMessage(`${e}`);
    }
  };

  const user = useFetchUser();

  return (
    <MainContainer>
      {user ? <p>Welcome, {user.userOid}!</p> : null}
      <OphTypography>{message}</OphTypography>
      <OphButton onClick={onGetPing}>HAE</OphButton>
      <RaporttiLinksList raportit={TOISEN_ASTEEN_RAPORTIT} />
    </MainContainer>
  );
}
