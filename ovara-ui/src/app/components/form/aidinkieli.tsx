import { useTranslate } from '@tolgee/react';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';
import { OvaraCheckboxGroup } from '@/app/components/form/OvaraCheckboxGroup';

export const Aidinkieli = () => {
  const { t } = useTranslate();
  const tasot = ['fi', 'sv', 'muu'];

  const { selectedAidinkieli, setSelectedAidinkieli } =
    useHakeneetSearchParams();

  return (
    <OvaraCheckboxGroup
      id={'aidinkieli'}
      options={tasot}
      selectedValues={selectedAidinkieli}
      setSelectedValues={setSelectedAidinkieli}
      t={t}
    />
  );
};
