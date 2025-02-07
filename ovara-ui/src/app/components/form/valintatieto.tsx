import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { OvaraCheckboxGroup } from './OvaraCheckboxGroup';

export const Valintatieto = ({ t }: { t: (key: string) => string }) => {
  const { data: valintatiedot } = useQuery({
    queryKey: ['fetchValintatiedot'],
    queryFn: () => doApiFetch('valintatiedot'),
  });

  const { selectedValintatieto, setSelectedValintatieto } =
    useHakijatSearchParams();

  // TODO: Poista tämä sitten kun kannasta tulee pelkkiä upper case arvoja
  const correctedValintatiedot = valintatiedot?.filter(
    (valintatieto) => valintatieto === valintatieto.toUpperCase(),
  );

  return (
    <OvaraCheckboxGroup
      id={'valintatieto'}
      options={correctedValintatiedot}
      selectedValues={selectedValintatieto}
      setSelectedValues={setSelectedValintatieto}
      t={t}
    />
  );
};
