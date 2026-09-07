import { ComboBox, SelectOption } from '@/app/components/form/multicombobox';
import { useFetchHakukohderyhmatForHaku } from '@/app/hooks/useFetchHakukohderyhmatForHaku';
import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';
import { Kielistetty, LanguageCode } from '@/app/lib/types/common';
import { useTranslate } from '@tolgee/react';
import { isNullish } from 'remeda';

type Hakukohderyhma = {
  hakukohderyhma_oid: string;
  hakukohderyhma_nimi: Kielistetty;
};

type Props = {
  hakuOid: string | null;
  value: string | null;
  onChange: (value: string | null) => void;
  disabled?: boolean;
};

/**
 * Single-select variant of `Hakukohderyhma`. Fetches hakukohderyhmat scoped to the
 * given `hakuOid` (via `useFetchHakukohderyhmatForHaku`) — not the shared
 * `useCommonSearchParams` state. Options are empty until `hakuOid` is set.
 */
export const HakukohderyhmaSingle = ({
  hakuOid,
  value,
  onChange,
  disabled,
}: Props) => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';

  const { data } = useFetchHakukohderyhmatForHaku(hakuOid);
  const hakukohderyhmat: Hakukohderyhma[] = data || [];

  const options: SelectOption[] = hakukohderyhmat.map((hkr) => ({
    value: hkr.hakukohderyhma_oid,
    label: hkr.hakukohderyhma_nimi[locale]
      ? `${hkr.hakukohderyhma_nimi[locale]}`
      : '',
  }));

  return (
    <ComboBox
      id="hakukohderyhma-single"
      label={t('raportti.hakukohderyhma')}
      value={disabled ? undefined : (value ?? undefined)}
      options={disabled ? [] : options}
      onChange={(_, selected) =>
        onChange(isNullish(selected) ? null : selected.value)
      }
    />
  );
};
