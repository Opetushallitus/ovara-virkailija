import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { isNullishOrEmpty } from '../lib/utils';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';

const fetchHakukohderyhmat = (selectedHaut: Array<string> | null) => {
  const paramsStr = `?haku=${selectedHaut?.toString()}`;
  return doApiFetch('hakukohderyhmat', {
    queryParams: paramsStr ?? null,
  });
};

export const useFetchHakukohderyhmat = () => {
  const { selectedHaut } = useCommonSearchParams();

  const fetchEnabled = !isNullishOrEmpty(selectedHaut);
  return useQuery({
    queryKey: ['fetchHakukohderyhmat', selectedHaut],
    queryFn: () => fetchHakukohderyhmat(selectedHaut),
    enabled: fetchEnabled,
  });
};
