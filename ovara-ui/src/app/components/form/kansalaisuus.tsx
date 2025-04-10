import { OvaraCheckboxGroup } from '@/app/components/form/OvaraCheckboxGroup';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { useTranslate } from '@tolgee/react';
import { getKansalaisuusTranslation } from '@/app/lib/utils';

export const Kansalaisuus = () => {
  const { t } = useTranslate();
  const kansalaisuusSelection = ['1', '2', '3'];

  const { selectedKansalaisuusluokat, setSelectedKansalaisuusluokat } =
    useCommonSearchParams();

  return (
    <OvaraCheckboxGroup
      id={'kansalaisuus'}
      options={kansalaisuusSelection}
      selectedValues={selectedKansalaisuusluokat}
      setSelectedValues={setSelectedKansalaisuusluokat}
      t={t}
      getTranslation={getKansalaisuusTranslation}
    />
  );
};
