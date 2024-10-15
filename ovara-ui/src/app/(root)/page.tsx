'use client';

import { useEffect, useState } from 'react';

import { OphButton, OphTypography } from '@opetushallitus/oph-design-system';
import { getPing } from '../lib/ovara-backend';
import { MainContainer } from '../components/main-container';
import { ListTable } from '../components/table/table';
import { TOISEN_ASTEEN_RAPORTIT } from '@/app/lib/constants';

type User = {
  userOid: string;
};

type UserResponse = {
  user: User;
};

type RaporttiList = {
  raportit: Array<string>;
};

const RaporttiLinksList = ({ raportit }: RaporttiList) => {
  return <ListTable list={raportit} />;
};

export default function Home() {
  const [message, setMessage] = useState('testi');
  const [user, setUser] = useState<User | null>(null);

  const onGetPing = async () => {
    try {
      const res = await getPing();
      setMessage(res.data as string);
    } catch (e) {
      setMessage(`${e}`);
    }
  };

  useEffect(() => {
    async function ensureLoggedIn() {
      const response = await fetch('/ovara-backend/api/user', {
        credentials: 'include',
      });
      const { user }: UserResponse = await response.json();
      const isLoggedIn = user !== null;
      if (!isLoggedIn) {
        location.assign('/ovara-backend/api/login');
      }
      setUser(user);
    }
    ensureLoggedIn();
  }, []);

  return (
    <MainContainer>
      {user ? <p>Welcome, {user.userOid}!</p> : null}
      <OphTypography>{message}</OphTypography>
      <OphButton onClick={onGetPing}>HAE</OphButton>
      <RaporttiLinksList raportit={TOISEN_ASTEEN_RAPORTIT} />
    </MainContainer>
  );
}
