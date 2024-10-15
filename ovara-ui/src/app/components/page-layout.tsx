import { Box } from '@mui/material';
import { PageContent } from './page-content';

export const PageLayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <Box
      width="100%"
      display="flex"
      flexDirection="column"
      rowGap={4}
      alignItems="stretch"
    >
      <PageContent>{children}</PageContent>
    </Box>
  );
};
