import { MultiComboBox } from '@/app/components/form/multicombobox';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { useTranslate } from '@tolgee/react';
import { useSearchParams } from 'next/navigation';
import { changeMultiComboBoxSelection } from '@/app/components/form/utils';
import { isNullish } from 'remeda';
import { useFetchHakukohderyhmat } from '@/app/hooks/useHakukohderyhmat';
import { useAsiointiKieli } from '@/app/hooks/useAsiointikieli';
import { Kielistetty } from '@/app/lib/types/common';

type Hakukohderyhma = {
  hakukohderyhma_oid: string;
  hakukohderyhma_nimi: Kielistetty;
};

export const Hakukohderyhma = () => {
  const { t } = useTranslate();
  const locale = useAsiointiKieli();

  const { selectedHakukohderyhmat, setSelectedHakukohderyhmat } =
    useCommonSearchParams();
  const queryParams = useSearchParams();
  const haut = queryParams.get('haku');
  const fetchEnabled = !isNullish(haut);
  const queryParamsStr = queryParams.toString();

  const { data: hakukohderyhmat } = useFetchHakukohderyhmat(
    queryParamsStr,
    fetchEnabled,
  );

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
