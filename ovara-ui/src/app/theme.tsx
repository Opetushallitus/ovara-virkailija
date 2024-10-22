'use client';
import { forwardRef } from 'react';
import { ophColors } from '@opetushallitus/oph-design-system';
import { styled as muiStyled, ThemeOptions } from '@mui/material/styles';
import { shouldForwardProp } from '@mui/system/createStyled';

export { ophColors } from '@opetushallitus/oph-design-system';

const withTransientProps = (propName: string) =>
  // Emotion doesn't support transient props by default so add support manually
  shouldForwardProp(propName) && !propName.startsWith('$');

export const DEFAULT_BOX_BORDER = `2px solid ${ophColors.grey100}`;
export const styled: typeof muiStyled = (
  tag: Parameters<typeof muiStyled>[0],
  options: Parameters<typeof muiStyled>[1] = {},
) => {
  return muiStyled(tag, {
    shouldForwardProp: (propName: string) =>
      (!options.shouldForwardProp || options.shouldForwardProp(propName)) &&
      withTransientProps(propName),
    ...options,
  });
};

export const THEME_OVERRIDES: ThemeOptions = {
  components: {
    MuiInputBase: {
      styleOverrides: {
        root: {
          borderColor: ophColors.grey800,
          borderRadius: '2px',
          height: '48px',
        },
      },
    },
    MuiLink: {
      styleOverrides: {
        root: {
          textDecoration: 'none',
          '&:hover, &:focus': {
            textDecoration: 'underline',
          },
        },
      },
    },
    MuiDialog: {
      defaultProps: {
        fullWidth: true,
      },
      styleOverrides: {
        paper: ({ theme }) => ({
          minHeight: '200px',
          borderTop: `4px solid ${ophColors.cyan1}`,
          borderRadius: '2px',
          boxShadow: '2px 2px 8px 0px rgba(0,0,0,0.17)',
          padding: theme.spacing(3),
        }),
      },
    },
    MuiDialogTitle: {
      defaultProps: {
        variant: 'h2',
      },
      styleOverrides: {
        root: ({ theme }) => ({
          padding: theme.spacing(0, 0, 2, 0),
        }),
      },
    },
    MuiDialogContent: {
      styleOverrides: {
        root: {
          padding: 0,
        },
      },
    },
    MuiDialogActions: {
      styleOverrides: {
        root: ({ theme }) => ({
          padding: theme.spacing(2, 0, 0, 0),
        }),
      },
    },
    MuiCircularProgress: {
      defaultProps: {
        size: 50,
        thickness: 4.5,
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { borderRadius: 'unset', fontSize: '1rem' },
      },
    },
  },
};

// MUI:sta (Emotionista) puuttuu styled-componentsin .attrs
// Tällä voi asettaa oletus-propsit ilman, että tarvii luoda välikomponenttia
export function withDefaultProps<P>(
  Component: React.ComponentType<P>,
  defaultProps: Partial<P>,
  displayName = 'ComponentWithDefaultProps',
) {
  const ComponentWithDefaultProps = forwardRef<
    React.ComponentRef<React.ComponentType<P>>,
    P
  >((props, ref) => <Component {...defaultProps} {...props} ref={ref} />);

  ComponentWithDefaultProps.displayName = displayName;
  return ComponentWithDefaultProps;
}
