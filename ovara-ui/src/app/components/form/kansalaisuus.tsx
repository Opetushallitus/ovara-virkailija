import { OvaraCheckboxGroup } from '@/app/components/form/OvaraCheckboxGroup';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { useTranslate } from '@tolgee/react';
import { getKansalaisuusTranslation } from '@/app/lib/utils';

export const Kansalaisuus = () => {
  const { t } = useTranslate();
  const kansalaisuusSelection = ['1', '2', '3'];

  const { selectedKansalaisuus, setSelectedKansalaisuus } =
    useCommonSearchParams();

  return (
    <OvaraCheckboxGroup
      id={'kansalaisuus'}
      options={kansalaisuusSelection}
      selectedValues={selectedKansalaisuus}
      setSelectedValues={setSelectedKansalaisuus}
      t={t}
      getTranslation={getKansalaisuusTranslation}
    />
  );
};
