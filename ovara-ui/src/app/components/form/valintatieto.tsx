import { useTranslate } from '@tolgee/react';
import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { OvaraCheckboxGroup } from './OvaraCheckboxGroup';

export const Valintatieto = ({ ...props }: { [key: string]: unknown }) => {
  const { t } = useTranslate();

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
