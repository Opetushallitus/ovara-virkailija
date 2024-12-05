'use client';
import { HomeOutlined as HomeOutlinedIcon } from '@mui/icons-material';
import { ophColors } from '@/app/theme';
import { Typography } from '@mui/material';
import { PageContent } from './page-content';
import { OphButton } from '@opetushallitus/oph-design-system';

import { useTranslations } from 'next-intl';
import { usePathname } from 'next/navigation';
const DEFAULT_BOX_BORDER = `2px solid ${ophColors.grey100}`;

export default function Header() {
  const t = useTranslations('header');
  const currentRoute = usePathname();
  const isHome = currentRoute == '/';
  const headerTranslationKey = isHome
    ? 'home'
    : currentRoute.match(/([a-z]+-?)+/gm)?.join('.');
  return (
    <header
      style={{
        position: 'relative',
        backgroundColor: ophColors.white,
        width: '100%',
        border: DEFAULT_BOX_BORDER,
      }}
    >
      <PageContent
        sx={{
          paddingY: 2,
          display: 'flex',
          alignItems: 'center',
          columnGap: 2,
        }}
      >
        {!isHome && (
          <OphButton
            href="/"
            variant="outlined"
            startIcon={<HomeOutlinedIcon />}
          />
        )}
        <Typography variant="h1">
          {isHome ? '' : '> '}
          {t(headerTranslationKey)}
        </Typography>
      </PageContent>
    </header>
  );
}
