import { ComboBox, SelectOption } from '@/app/components/form/multicombobox';
import { useFetchHakukohteetForHaku } from '@/app/hooks/useFetchHakukohteetForHaku';
import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';
import { Kielistetty, LanguageCode } from '@/app/lib/types/common';
import { useTranslate } from '@tolgee/react';
import { isNullish } from 'remeda';

type Hakukohde = {
  hakukohde_oid: string;
  hakukohde_nimi: Kielistetty;
};

type Props = {
  hakuOid: string | null;
  value: string | null;
  onChange: (value: string | null) => void;
  disabled?: boolean;
};

/**
 * Single-select variant of `Hakukohde`. Fetches hakukohteet scoped to the
 * given `hakuOid` (via `useFetchHakukohteetForHaku`) — not the shared
 * `useCommonSearchParams` state. Options are empty until `hakuOid` is set.
 */
export const HakukohdeSingle = ({
  hakuOid,
  value,
  onChange,
  disabled,
}: Props) => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';

  const { data } = useFetchHakukohteetForHaku(hakuOid);
  const hakukohteet: Hakukohde[] = data || [];

  const options: SelectOption[] = hakukohteet.map((hk) => ({
    value: hk.hakukohde_oid,
    label: hk.hakukohde_nimi[locale] ? `${hk.hakukohde_nimi[locale]}` : '',
  }));

  return (
    <ComboBox
      id="hakukohde-single"
      label={t('raportti.hakukohde')}
      value={disabled ? undefined : (value ?? undefined)}
      options={disabled ? [] : options}
      onChange={(_, selected) =>
        onChange(isNullish(selected) ? null : selected.value)
      }
    />
  );
};
