import { useTranslate } from '@tolgee/react';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';
import { OvaraCheckboxGroup } from '@/app/components/form/OvaraCheckboxGroup';

export const Aidinkieli = () => {
  const { t } = useTranslate();
  const tasot = ['fi', 'sv', 'muu'];

  const { selectedAidinkielet, setSelectedAidinkielet } =
    useHakeneetSearchParams();

  return (
    <OvaraCheckboxGroup
      id={'aidinkieli'}
      options={tasot}
      selectedValues={selectedAidinkielet}
      setSelectedValues={setSelectedAidinkielet}
      t={t}
    />
  );
};
