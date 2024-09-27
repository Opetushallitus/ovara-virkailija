'use client';

import { useState } from 'react';

import { OphButton, OphTypography } from '@opetushallitus/oph-design-system';
import { getPing } from '../lib/ovara-backend';

export default function Home() {
  const [message, setMessage] = useState('testi');

  const onGetPing = async () => {
    try {
      const res = await getPing();
      setMessage(res.data);
    } catch (e) {
      setMessage(`${e}`);
    }
  };

  return (
    <div>
      <main>
        <OphTypography>{message}</OphTypography>
        <OphButton onClick={onGetPing}>HAE</OphButton>
      </main>
    </div>
  );
}
