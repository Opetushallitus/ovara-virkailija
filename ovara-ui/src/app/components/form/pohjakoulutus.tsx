import { MultiComboBox } from '@/app/components/form/multicombobox';
import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { changeMultiComboBoxSelection } from './utils';

export const Pohjakoulutus = ({
  t,
  locale,
}: {
  t: (key: string) => string;
  locale: string;
}) => {
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
        pohjakoulutukset?.map((pohjakoulutus) => {
          return {
            value: pohjakoulutus.koodiarvo,
            label: pohjakoulutus.koodinimi[locale] || '',
          };
        }) || []
      }
      onChange={(e, value) =>
        changeMultiComboBoxSelection(e, value, setSelectedPohjakoulutukset)
      }
      required={true}
    />
  );
};
