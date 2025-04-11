import { MultiComboBox } from '@/app/components/form/multicombobox';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { useTranslate } from '@tolgee/react';
import { changeMultiComboBoxSelection } from '@/app/components/form/utils';
import { useAsiointiKieli } from '@/app/hooks/useAsiointikieli';
import { Kielistetty } from '@/app/lib/types/common';
import { useFetchHakukohderyhmat } from '@/app/hooks/useFetchHakukohderyhmat';

type Hakukohderyhma = {
  hakukohderyhma_oid: string;
  hakukohderyhma_nimi: Kielistetty;
};

export const Hakukohderyhma = () => {
  const { t } = useTranslate();
  const locale = useAsiointiKieli();

  const { selectedHakukohderyhmat, setSelectedHakukohderyhmat } =
    useCommonSearchParams();

  const { data: hakukohderyhmat } = useFetchHakukohderyhmat();

  return (
    <MultiComboBox
      id={'hakukohderyhma'}
      label={t('raportti.hakukohderyhma')}
      value={selectedHakukohderyhmat ?? []}
      options={
        hakukohderyhmat?.map((hakukohderyhma: Hakukohderyhma) => {
          return {
            value: hakukohderyhma.hakukohderyhma_oid,
            label: hakukohderyhma.hakukohderyhma_nimi[locale] || '',
          };
        }) || []
      }
      onChange={(e, value) =>
        changeMultiComboBoxSelection(e, value, setSelectedHakukohderyhmat)
      }
    />
  );
};
