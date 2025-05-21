import { Koodi, LanguageCode } from '@/app/lib/types/common';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { MultiComboBox } from '@/app/components/form/multicombobox';
import { Box } from '@mui/material';
import {
  changeMultiComboBoxSelection,
  getKoodiOptions,
} from '@/app/components/form/utils';

export const MaakuntaKuntaValikot = () => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';

  const {
    selectedMaakunnat,
    setSelectedMaakunnat,
    selectedKunnat,
    setSelectedKunnat,
  } = useHakeneetSearchParams();

  const maakunnatData = useQuery({
    queryKey: ['fetchMaakunnat'],
    queryFn: () => doApiFetch('maakunnat'),
  });

  const kunnatData = useQuery({
    queryKey: ['fetchKunnat', selectedMaakunnat, selectedKunnat],
    queryFn: () =>
      doApiFetch('kunnat', {
        queryParams: selectedMaakunnat
          ? `?maakunnat=${selectedMaakunnat}&selectedKunnat=${selectedKunnat ? selectedKunnat : []}`
          : null,
      }),
  });

  const maakunnat: Array<Koodi> = maakunnatData.data || [];
  const kunnat: Array<Koodi> = kunnatData.data || [];

  const maakunnat_id = 'maakunnat';
  const kunnat_id = 'kunnat';

  return (
    <Box>
      <MultiComboBox
        id={maakunnat_id}
        label={t(`raportti.${maakunnat_id}`)}
        value={selectedMaakunnat ?? []}
        options={getKoodiOptions(locale, maakunnat)}
        onChange={(e, value) =>
          changeMultiComboBoxSelection(e, value, setSelectedMaakunnat)
        }
      />
      <MultiComboBox
        id={kunnat_id}
        label={t(`raportti.${kunnat_id}`)}
        value={selectedKunnat ?? []}
        options={getKoodiOptions(locale, kunnat)}
        onChange={(e, value) =>
          changeMultiComboBoxSelection(e, value, setSelectedKunnat)
        }
      />
    </Box>
  );
};
