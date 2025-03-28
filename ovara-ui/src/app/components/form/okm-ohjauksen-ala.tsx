import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { Koodi, LanguageCode } from '@/app/lib/types/common';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { MultiComboBox } from '@/app/components/form/multicombobox';
import {
  changeMultiComboBoxSelection,
  getKoodiOptions,
} from '@/app/components/form/utils';

export const OkmOhjauksenAlat = () => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';

  const { selectedOkmOhjauksenAlat, setSelectedOkmOhjauksenAlat } =
    useHakeneetSearchParams();

  const okmOhjauksenAlaData = useQuery({
    queryKey: ['fetchOkmOhjauksenAlat'],
    queryFn: () => doApiFetch('okm-ohjauksen-alat'),
  });

  const maakunnat: Array<Koodi> = okmOhjauksenAlaData.data || [];

  const okm_ohjauksen_alat_id = 'okm-ohjauksen-alat';

  return (
    <MultiComboBox
      id={okm_ohjauksen_alat_id}
      label={t(`raportti.${okm_ohjauksen_alat_id}`)}
      value={selectedOkmOhjauksenAlat ?? []}
      options={getKoodiOptions(locale, maakunnat)}
      onChange={(e, value) =>
        changeMultiComboBoxSelection(e, value, setSelectedOkmOhjauksenAlat)
      }
    />
  );
};
