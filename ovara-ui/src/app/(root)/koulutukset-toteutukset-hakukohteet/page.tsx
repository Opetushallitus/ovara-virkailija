'use client';
import { Typography } from '@mui/material';
import { MainContainer } from '@/app/components/main-container';
import { useTranslations } from 'next-intl';

import { KoulutuksenAlkaminen } from '@/app/components/form/koulutuksen-alkaminen';
import { Haku } from '@/app/components/form/haku';

export default function KoulutuksetToteutuksetHakukohteet() {
  const t = useTranslations();
  return (
    <MainContainer>
      <Typography>{t('yleinen.pakolliset-kentat')}</Typography>
      <KoulutuksenAlkaminen t={t} />
      <Haku t={t} />
    </MainContainer>
  );
}
