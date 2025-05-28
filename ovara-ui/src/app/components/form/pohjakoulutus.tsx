import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';
import { MultiComboBox } from '@/app/components/form/multicombobox';
import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { changeMultiComboBoxSelection } from './utils';
import { Koodi, LanguageCode } from '@/app/lib/types/common';

export const Pohjakoulutus = () => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';

  const { data: pohjakoulutukset } = useQuery({
    queryKey: ['fetchPohjakoulutukset'],
    queryFn: () => doApiFetch('pohjakoulutukset-toinen-aste'),
  });

  const { selectedPohjakoulutukset, setSelectedPohjakoulutukset } =
    useHakijatSearchParams();

  return (
    <MultiComboBox
      id={'pohjakoulutus'}
      label={t('raportti.pohjakoulutus')}
      value={selectedPohjakoulutukset ?? []}
      options={
        pohjakoulutukset?.map((pohjakoulutus: Koodi) => {
          return {
            value: pohjakoulutus.koodiarvo,
            label: pohjakoulutus.koodinimi[locale as LanguageCode] || '',
          };
        }) || []
      }
      onChange={(e, value) =>
        changeMultiComboBoxSelection(e, value, setSelectedPohjakoulutukset)
      }
    />
  );
};
