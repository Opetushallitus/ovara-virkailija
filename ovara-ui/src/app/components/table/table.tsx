'use client';

import {
  Box,
  Link,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  styled,
} from '@mui/material';
import { ophColors } from '@/app/theme';
import { useTranslations } from 'next-intl';

export const StyledTable = styled(Table)({
  width: '100%',
  borderSpacing: '0px',
});

const StyledCell = styled(TableCell)({
  borderSpacing: '0px',
  textAlign: 'left',
  whiteSpace: 'pre-wrap',
  borderWidth: 0,
  'button:focus': {
    color: ophColors.blue2,
  },
});

const StyledTableBody = styled(TableBody)({
  '& .MuiTableRow-root': {
    '&:nth-of-type(even)': {
      backgroundColor: ophColors.grey50,
      '.MuiTableCell-root': {
        backgroundColor: ophColors.grey50,
      },
    },
    '&:nth-of-type(odd)': {
      backgroundColor: ophColors.white,
      '.MuiTableCell-root': {
        backgroundColor: ophColors.white,
      },
    },
    '&:hover': {
      backgroundColor: ophColors.lightBlue2,
    },
  },
});

interface ListTableProps extends React.ComponentProps<typeof StyledTable> {
  list: Array<string>;
}

const StyledHeaderCell = styled(TableCell)({
  padding: '0.2rem 0.1rem 0.2rem 0.4rem',
  textAlign: 'left',
  'button:focus': {
    color: ophColors.blue2,
  },
});

const TableWrapper = styled(Box)(({ theme }) => ({
  position: 'relative',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  width: '100%',
  overflowX: 'auto',
  rowGap: theme.spacing(1),
}));

export const ListTable = ({ list, ...props }: ListTableProps) => {
  const t = useTranslations('Raporttilista');

  return (
    <TableWrapper>
      <StyledTable {...props}>
        <TableHead>
          <TableRow sx={{ borderBottom: `2px solid ${ophColors.grey200}` }}>
            <StyledHeaderCell>{t('title')}</StyledHeaderCell>
          </TableRow>
        </TableHead>
        <StyledTableBody>
          {list.map((key) => {
            return (
              <TableRow key={key}>
                <StyledCell>
                  <Link href={`/${key}`} sx={{ textDecoration: 'none' }}>
                    {t(key)}
                  </Link>
                </StyledCell>
              </TableRow>
            );
          })}
        </StyledTableBody>
      </StyledTable>
    </TableWrapper>
  );
};
