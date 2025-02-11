import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { SUKUPUOLET } from '@/app/lib/constants';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';

export const Sukupuoli = ({ t }: { t: (key: string) => string }) => {
  const { selectedSukupuoli, setSelectedSukupuoli } = useHakeneetSearchParams();

  return (
    <OvaraRadioGroup
      label={t(`raportti.sukupuoli`)}
      options={SUKUPUOLET}
      value={selectedSukupuoli}
      onChange={(e) => setSelectedSukupuoli(e.target.value)}
    />
  );
};
