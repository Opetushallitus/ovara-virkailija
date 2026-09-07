import { ComboBox, SelectOption } from '@/app/components/form/multicombobox';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { useFetchHaut } from '@/app/hooks/useFetchHaut';
import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';
import { Kielistetty, LanguageCode } from '@/app/lib/types/common';
import { useTranslate } from '@tolgee/react';
import { isNullish } from 'remeda';
import { useEffect } from 'react';

type Haku = {
  haku_oid: string;
  haku_nimi: Kielistetty;
};

type Props = {
  haunTyyppi: string;
  value: string | null;
  onChange: (value: string | null) => void;
  required?: boolean;
};

/**
 * Single-select variant of `Haku`. Uses the same shared `useFetchHaut` hook
 * (which depends on the `KoulutuksenAlkaminen` selection in common state)
 * but manages the selected value via caller-provided props instead of the
 * global `selectedHaut` array.
 */
export const HakuSingle = ({
  haunTyyppi,
  value,
  onChange,
  required,
}: Props) => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli ?? 'fi') as LanguageCode;

  const { setHauntyyppi } = useCommonSearchParams();

  useEffect(() => {
    setHauntyyppi(haunTyyppi);
  }, [haunTyyppi, setHauntyyppi]);

  const { data } = useFetchHaut();
  const haut: Haku[] = data || [];

  const options: SelectOption[] = haut.map((haku) => ({
    value: haku.haku_oid,
    label: haku.haku_nimi[locale] || '',
  }));

  return (
    <ComboBox
      id="haku-single"
      label={t('raportti.haku')}
      value={value ?? undefined}
      options={options}
      onChange={(_, selected) =>
        onChange(isNullish(selected) ? null : selected.value)
      }
      required={required}
    />
  );
};
