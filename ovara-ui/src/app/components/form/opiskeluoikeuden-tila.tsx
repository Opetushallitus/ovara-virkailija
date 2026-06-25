import { useTranslate } from '@tolgee/react';
import { usePaatettavatOpiskeluoikeudetSearchParams } from '@/app/hooks/searchParams/usePaatettavatOpiskeluoikeudetSearchParams';
import { OvaraRadioGroup } from '@/app/components/form/ovara-radio-group';
import { OPISKELUOIKEUDEN_TILAT } from '@/app/lib/constants';

export const OpiskeluoikeudenTila = () => {
  const { t } = useTranslate();
  const { selectedOpiskeluoikeudenTila, setSelectedOpiskeluoikeudenTila } =
    usePaatettavatOpiskeluoikeudetSearchParams();

  const opiskeluoikeudenTilaLabels = {
    kaikki: t('raportti.kaikki'),
    paatettavissa: t('raportti.voimassa-paatettavissa'),
    paatetty: t('raportti.ei-voimassa-paatetty'),
  };

  const getNonNullSelectedValue = (selected: string | null) =>
    selected === null ? 'kaikki' : selected;

  return (
    <OvaraRadioGroup
      label={t(`raportti.opiskeluoikeuden-tila`)}
      options={OPISKELUOIKEUDEN_TILAT}
      value={getNonNullSelectedValue(selectedOpiskeluoikeudenTila)}
      labels={opiskeluoikeudenTilaLabels}
      onChange={(e) => setSelectedOpiskeluoikeudenTila(e.target.value)}
    />
  );
};
