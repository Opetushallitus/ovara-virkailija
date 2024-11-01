'use client';
import { Typography } from '@mui/material';
import { MainContainer } from '@/app/components/main-container';
import { useTranslations } from 'next-intl';

import { KoulutuksenAlkaminen } from '@/app/components/form/koulutuksen-alkaminen';
import { Haku } from '@/app/components/form/haku';
import {
  HakukohteenTila,
  KoulutuksenTila,
  ToteutuksenTila,
} from '@/app/components/form/tila';
import { Valintakoe } from '@/app/components/form/valintakoe';

export default function KoulutuksetToteutuksetHakukohteet() {
  const t = useTranslations();
  return (
    <MainContainer>
      <Typography>{t('yleinen.pakolliset-kentat')}</Typography>
      <KoulutuksenAlkaminen />
      <Haku />
      <KoulutuksenTila />
      <ToteutuksenTila />
      <HakukohteenTila />
      <Valintakoe />
    </MainContainer>
  );
}
