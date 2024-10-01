'use client';

import { useEffect, useState } from 'react';

import { OphButton, OphTypography } from '@opetushallitus/oph-design-system';
import { getPing } from '../lib/ovara-backend';

export default function Home() {
  const [message, setMessage] = useState('testi');
  const [user, setUser] = useState(null);

  const onGetPing = async () => {
    try {
      const res = await getPing();
      setMessage(res.data);
    } catch (e) {
      setMessage(`${e}`);
    }
  };

  useEffect(() => {
    async function ensureLoggedIn() {
      const response = await fetch('/ovara-backend/api/user', {
        credentials: 'include',
      });
      const { user } = await response.json();
      const isLoggedIn = user !== null;
      if (!isLoggedIn) {
        location.assign('/ovara-backend/api/login');
      }
      setUser(user);
    }
    ensureLoggedIn();
  }, []);

  return (
    <div>
      <main>
        {user ? <p>Welcome, {user.username}!</p> : null}
        <OphTypography>{message}</OphTypography>
        <OphButton onClick={onGetPing}>HAE</OphButton>
      </main>
    </div>
  );
}
