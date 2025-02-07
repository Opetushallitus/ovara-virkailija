import { ophColors } from '@opetushallitus/oph-design-system';
import { styled } from '@mui/material';

export const FormBox = styled('form')(({ theme }) => ({
  border: `1px solid ${ophColors.grey100}`,
  padding: theme.spacing(2.5),
  width: '100%',
}));
