import { useSearchParams } from '@/app/hooks/searchParams/useSearchParams';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { getHarkinnanvaraisuusTranslation } from '@/app/lib/utils';
import { OvaraCheckboxGroup } from './OvaraCheckboxGroup';

export const Harkinnanvaraisuus = ({ t }: { t: (key: string) => string }) => {
  const { data: harkinnanvaraisuudet } = useQuery({
    queryKey: ['fetchHarkinnanvaraisuudet'],
    queryFn: () => doApiFetch('harkinnanvaraisuudet'),
  });

  const { selectedHarkinnanvaraisuus, setSelectedHarkinnanvaraisuus } =
    useSearchParams();

  return (
    <OvaraCheckboxGroup
      id={'harkinnanvaraisuus'}
      options={harkinnanvaraisuudet}
      selectedValues={selectedHarkinnanvaraisuus}
      setSelectedValues={setSelectedHarkinnanvaraisuus}
      t={t}
      getTranslation={getHarkinnanvaraisuusTranslation}
    />
  );
};
