import {
  MultiComboBox,
  SelectOption,
} from '@/app/components/form/multicombobox';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { useTranslate } from '@tolgee/react';
import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';
import { Kielistetty, LanguageCode } from '@/app/lib/types/common';
import { useFetchHaut } from '@/app/hooks/useFetchHaut';
import { changeMultiComboBoxSelection } from '@/app/components/form/utils';
import { useEffect } from 'react';

type Haku = {
  haku_oid: string;
  haku_nimi: Kielistetty;
};

export const Haku = ({ haunTyyppi }: { haunTyyppi: string }) => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli ?? 'fi') as LanguageCode;

  const {
    selectedHaut,
    setSelectedHaut,
    setSelectedHakukohderyhmat,
    setHauntyyppi,
  } = useCommonSearchParams();

  useEffect(() => {
    setHauntyyppi(haunTyyppi);
  }, [haunTyyppi, setHauntyyppi]);

  const { data } = useFetchHaut();

  const haut: Haku[] = data || [];

  const changeHaku = (_: React.SyntheticEvent, value: Array<SelectOption>) => {
    // tyhjennetään hakukohderyhmävalinta jos haku muuttuu
    setSelectedHakukohderyhmat(null);
    changeMultiComboBoxSelection(_, value, setSelectedHaut);
  };

  return (
    <MultiComboBox
      id={'haku'}
      label={t('raportti.haku')}
      value={selectedHaut ?? []}
      options={haut?.map((haku) => {
        return {
          value: haku.haku_oid,
          label: haku.haku_nimi[locale] || '',
        };
      })}
      onChange={(e, value) => changeHaku(e, value)}
      required={true}
    />
  );
};
