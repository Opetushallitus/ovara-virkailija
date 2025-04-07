'use client';
import { ComponentRef, ComponentType, forwardRef } from 'react';
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
    MuiButton: {
      defaultProps: {
        loadingPosition: 'start',
      },
    },
    MuiDivider: {
      styleOverrides: {
        root: ({ theme }) => ({
          margin: theme.spacing(3, 0, 3, 0),
        }),
      },
    },
    MuiAutocomplete: {
      styleOverrides: {
        root: {
          padding: '5px', // lisätään vähän paddingia jotta on tilaa focusreunukselle
        },
      },
    },
  },
};

// MUI:sta (Emotionista) puuttuu styled-componentsin .attrs
// Tällä voi asettaa oletus-propsit ilman, että tarvii luoda välikomponenttia
/* eslint-disable @typescript-eslint/no-explicit-any */
export function withDefaultProps<P extends React.ComponentPropsWithoutRef<any>>(
  Component: ComponentType<P>,
  defaultProps: Partial<P>,
  displayName = 'ComponentWithDefaultProps',
) {
  const ComponentWithDefaultProps = forwardRef<
    ComponentRef<ComponentType<P>>,
    P
  >((props, ref) => <Component {...defaultProps} {...props} ref={ref} />);

  ComponentWithDefaultProps.displayName = displayName;
  return ComponentWithDefaultProps;
}
