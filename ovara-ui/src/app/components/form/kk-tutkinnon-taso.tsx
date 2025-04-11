import { useTranslate } from '@tolgee/react';
import { OvaraCheckboxGroup } from '@/app/components/form/OvaraCheckboxGroup';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';

export const KkTutkinnonTaso = () => {
  const { t } = useTranslate();
  const tasot = ['alempi-ja-ylempi', 'alempi', 'ylempi'];

  const { selectedTutkinnonTasot, setSelectedTutkinnonTasot } =
    useHakeneetSearchParams();

  return (
    <OvaraCheckboxGroup
      id={'kk-tutkinnon-taso'}
      options={tasot}
      selectedValues={selectedTutkinnonTasot}
      setSelectedValues={setSelectedTutkinnonTasot}
      t={t}
    />
  );
};
