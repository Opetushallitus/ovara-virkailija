import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { OvaraCheckboxGroup } from './OvaraCheckboxGroup';

export const Valintatieto = ({
  t,
  ...props
}: {
  t: (key: string) => string;
  [key: string]: unknown;
}) => {
  const valintatiedotSelection = [
    'HYVAKSYTTY',
    'HYLATTY',
    'PERUUNTUNUT',
    'VARALLA',
  ];

  const { selectedValintatiedot, setSelectedValintatiedot } =
    useHakijatSearchParams();

  return (
    <OvaraCheckboxGroup
      id={'valintatieto'}
      options={valintatiedotSelection}
      selectedValues={selectedValintatiedot}
      setSelectedValues={setSelectedValintatiedot}
      t={t}
      {...props}
    />
  );
};
