'use client';
import { HomeOutlined as HomeOutlinedIcon } from '@mui/icons-material';
import { ophColors } from '@/app/theme';
import { Typography } from '@mui/material';
import { PageContent } from './page-content';
import { OphButton } from '@opetushallitus/oph-design-system';

const DEFAULT_BOX_BORDER = `2px solid ${ophColors.grey100}`;

export type HeaderProps = {
  title?: React.ReactNode;
  isHome?: boolean;
};

export default function Header({ title, isHome = false }: HeaderProps) {
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
          {title}
        </Typography>
      </PageContent>
    </header>
  );
}
