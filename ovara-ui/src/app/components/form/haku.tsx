import { MultiComboBox } from '@/app/components/form/multicombobox';
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

  const { selectedHaut, setSelectedHaut, setHauntyyppi } =
    useCommonSearchParams();

  useEffect(() => {
    setHauntyyppi(haunTyyppi);
  }, [haunTyyppi, setHauntyyppi]);

  const { data } = useFetchHaut();

  const haut: Haku[] = data || [];

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
      onChange={(e, value) =>
        changeMultiComboBoxSelection(e, value, setSelectedHaut)
      }
      required={true}
    />
  );
};
