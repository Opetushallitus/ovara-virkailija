import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { SUKUPUOLET } from '@/app/lib/constants';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';
import { useTranslate } from '@tolgee/react';

export const Sukupuoli = () => {
  const { t } = useTranslate();
  const { selectedSukupuoli, setSelectedSukupuoli } = useHakeneetSearchParams();

  const sukupuoliLabels = {
    neutral: t('raportti.sukupuoli.neutral'),
    '1': t('raportti.sukupuoli.mies'),
    '2': t('raportti.sukupuoli.nainen'),
  };

  const getNonNullSelectedValue = (selected: string | null) =>
    selected === null ? 'neutral' : selected;

  return (
    <OvaraRadioGroup
      label={t(`raportti.sukupuoli`)}
      options={SUKUPUOLET}
      value={getNonNullSelectedValue(selectedSukupuoli)}
      labels={sukupuoliLabels}
      onChange={(e) => setSelectedSukupuoli(e.target.value)}
    />
  );
};
