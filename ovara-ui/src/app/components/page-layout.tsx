import { Box } from '@mui/material';

export const PageLayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <Box
      width="100%"
      display="flex"
      flexDirection="column"
      rowGap={4}
      alignItems="stretch"
    >
      {children}
    </Box>
  );
};
