import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { OvaraCheckboxGroup } from './OvaraCheckboxGroup';

export const Vastaanottotieto = ({ t }: { t: (key: string) => string }) => {
  const { data: vastaanottoSelection } = useQuery({
    queryKey: ['fetchVastaanottotiedot'],
    queryFn: () => doApiFetch('vastaanottotiedot'),
  });
  const { selectedVastaanottotieto, setSelectedVastaanottotieto } =
    useHakijatSearchParams();

  // TODO: Poista tämä sitten kun kannasta tulee pelkkiä upper case arvoja
  const correctedVastaanottotiedot = vastaanottoSelection?.filter(
    (valintatieto: string) => valintatieto === valintatieto?.toUpperCase(),
  );

  return (
    <OvaraCheckboxGroup
      id={'valintatieto'}
      options={correctedVastaanottotiedot}
      selectedValues={selectedVastaanottotieto}
      setSelectedValues={setSelectedVastaanottotieto}
      t={t}
    />
  );
};
