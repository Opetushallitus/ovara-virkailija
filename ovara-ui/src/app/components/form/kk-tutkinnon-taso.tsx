import { useTranslate } from '@tolgee/react';
import { OvaraCheckboxGroup } from '@/app/components/form/OvaraCheckboxGroup';
import { useHakeneetSearchParams } from '@/app/hooks/searchParams/useHakeneetSearchParams';

export const KkTutkinnonTaso = () => {
  const { t } = useTranslate();
  const tasot = ['alempi-ja-ylempi', 'alempi', 'ylempi'];

  const { selectedTutkinnonTaso, setSelectedTutkinnonTaso } =
    useHakeneetSearchParams();

  return (
    <OvaraCheckboxGroup
      id={'kk-tutkinnon-taso'}
      options={tasot}
      selectedValues={selectedTutkinnonTaso}
      setSelectedValues={setSelectedTutkinnonTaso}
      t={t}
    />
  );
};
