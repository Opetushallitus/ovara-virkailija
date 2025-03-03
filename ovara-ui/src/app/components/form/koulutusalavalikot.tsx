import { Koodi, LanguageCode } from '@/app/lib/types/common';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { MultiComboBox } from '@/app/components/form/multicombobox';
import { Box } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';
import {
  changeMultiComboBoxSelection,
  getKoodiOptions,
} from '@/app/components/form/utils';

export const KoulutusalaValikot = () => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';

  const {
    selectedKoulutusalat1,
    setSelectedKoulutusalat1,
    selectedKoulutusalat2,
    setSelectedKoulutusalat2,
    selectedKoulutusalat3,
    setSelectedKoulutusalat3,
  } = useHakeneetSearchParams();

  const koulutusalat1data = useQuery({
    queryKey: ['fetchKoulutusalat1'],
    queryFn: () => doApiFetch('koulutusalat1'),
  });

  const koulutusalat2data = useQuery({
    queryKey: ['fetchKoulutusalat2', selectedKoulutusalat1],
    queryFn: () =>
      doApiFetch('koulutusalat2', {
        queryParams: selectedKoulutusalat1
          ? `?koulutusalat1=${selectedKoulutusalat1}`
          : null,
      }),
  });

  const koulutusalat3data = useQuery({
    queryKey: ['fetchKoulutusalat3', selectedKoulutusalat2],
    queryFn: () =>
      doApiFetch('koulutusalat3', {
        queryParams: selectedKoulutusalat1
          ? `?koulutusalat2=${selectedKoulutusalat2}`
          : null,
      }),
  });

  const koulutusalat1: Array<Koodi> = koulutusalat1data.data || [];
  const koulutusalat2: Array<Koodi> = koulutusalat2data.data || [];
  const koulutusalat3: Array<Koodi> = koulutusalat3data.data || [];

  const koulutusalat1_id = 'koulutusalat1';
  const koulutusalat2_id = 'koulutusalat2';
  const koulutusalat3_id = 'koulutusalat3';

  return (
    <Box>
      <MultiComboBox
        id={koulutusalat1_id}
        label={t(`raportti.${koulutusalat1_id}`)}
        value={selectedKoulutusalat1 ?? []}
        options={getKoodiOptions(locale, koulutusalat1)}
        onChange={(e, value) =>
          changeMultiComboBoxSelection(e, value, setSelectedKoulutusalat1)
        }
      />
      <MultiComboBox
        id={koulutusalat2_id}
        label={t(`raportti.${koulutusalat2_id}`)}
        value={selectedKoulutusalat2 ?? []}
        options={getKoodiOptions(locale, koulutusalat2)}
        onChange={(e, value) =>
          changeMultiComboBoxSelection(e, value, setSelectedKoulutusalat2)
        }
      />
      <MultiComboBox
        id={koulutusalat3_id}
        label={t(`raportti.${koulutusalat3_id}`)}
        value={selectedKoulutusalat3 ?? []}
        options={getKoodiOptions(locale, koulutusalat3)}
        onChange={(e, value) =>
          changeMultiComboBoxSelection(e, value, setSelectedKoulutusalat3)
        }
      />
    </Box>
  );
};
