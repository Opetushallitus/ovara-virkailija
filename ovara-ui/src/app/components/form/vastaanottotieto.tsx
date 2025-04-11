import { useTranslate } from '@tolgee/react';
import { useHakijatSearchParams } from '@/app/hooks/searchParams/useHakijatSearchParams';
import { OvaraCheckboxGroup } from './OvaraCheckboxGroup';

export const Vastaanottotieto = () => {
  const { t } = useTranslate();

  const { selectedVastaanottotiedot, setSelectedVastaanottotiedot } =
    useHakijatSearchParams();

  const toisenAsteenNaytettavatVastaanottotilat = [
    'PERUNUT',
    'PERUUTETTU',
    'VASTAANOTTANUT',
  ];

  return (
    <OvaraCheckboxGroup
      id={'vastaanottotieto'}
      options={toisenAsteenNaytettavatVastaanottotilat}
      selectedValues={selectedVastaanottotiedot}
      setSelectedValues={setSelectedVastaanottotiedot}
      t={t}
    />
  );
};
