import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { OvaraCheckboxGroup } from './OvaraCheckboxGroup';

export const Valintatieto = ({ t }: { t: (key: string) => string }) => {
  const valintatiedotSelection = [
    'HYVAKSYTTY',
    'HYLATTY',
    'PERUUNTUNUT',
    'VARALLA',
  ];

  const { selectedValintatieto, setSelectedValintatieto } =
    useHakijatSearchParams();

  return (
    <OvaraCheckboxGroup
      id={'valintatieto'}
      options={valintatiedotSelection}
      selectedValues={selectedValintatieto}
      setSelectedValues={setSelectedValintatieto}
      t={t}
    />
  );
};
