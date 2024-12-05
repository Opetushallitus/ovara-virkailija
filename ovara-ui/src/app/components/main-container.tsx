'use client';
import { styled } from '@mui/material';
import { Box, BoxProps } from '@mui/material';
import { ophColors, withDefaultProps, DEFAULT_BOX_BORDER } from '../theme';

export const MainContainer = withDefaultProps(
  styled(Box)(({ theme }) => ({
    padding: theme.spacing(4),
    border: DEFAULT_BOX_BORDER,
    backgroundColor: ophColors.white,
  })),
  {
    component: 'main',
  } as BoxProps,
) as typeof Box;
