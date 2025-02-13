import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { SUKUPUOLET } from '@/app/lib/constants';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';

export const Sukupuoli = ({ t }: { t: (key: string) => string }) => {
  const { selectedSukupuoli, setSelectedSukupuoli } = useHakeneetSearchParams();

  const sukupuoliLabels = {
    neutral: t('raportti.sukupuoli.neutral'),
    '1': t('raportti.sukupuoli.mies'),
    '2': t('raportti.sukupuoli.nainen'),
  };

  return (
    <OvaraRadioGroup
      label={t(`raportti.sukupuoli`)}
      options={SUKUPUOLET}
      value={selectedSukupuoli}
      labels={sukupuoliLabels}
      onChange={(e) => setSelectedSukupuoli(e.target.value)}
    />
  );
};
