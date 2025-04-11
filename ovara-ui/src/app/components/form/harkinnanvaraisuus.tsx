import { useTranslate } from '@tolgee/react';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { getHarkinnanvaraisuusTranslation } from '@/app/lib/utils';
import { OvaraCheckboxGroup } from './OvaraCheckboxGroup';

export const Harkinnanvaraisuus = () => {
  const { t } = useTranslate();
  const { data: harkinnanvaraisuudet } = useQuery({
    queryKey: ['fetchHarkinnanvaraisuudet'],
    queryFn: () => doApiFetch('harkinnanvaraisuudet'),
  });

  const { selectedHarkinnanvaraisuudet, setSelectedHarkinnanvaraisuudet } =
    useCommonSearchParams();

  return (
    <OvaraCheckboxGroup
      id={'harkinnanvaraisuus'}
      options={harkinnanvaraisuudet}
      selectedValues={selectedHarkinnanvaraisuudet}
      setSelectedValues={setSelectedHarkinnanvaraisuudet}
      t={t}
      getTranslation={getHarkinnanvaraisuusTranslation}
      boxSx={{ columns: 3, width: '100%' }}
    />
  );
};
