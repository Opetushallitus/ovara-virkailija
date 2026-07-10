import { ComboBox, SelectOption } from '@/app/components/form/multicombobox';
import { useFetchOrganisaatiohierarkiat } from '@/app/hooks/useFetchOrganisaatiohierarkiat';
import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';
import { LanguageCode, OrganisaatioHierarkia } from '@/app/lib/types/common';
import { getKoulutustoimijatToShow } from '@/app/lib/utils';
import { useTranslate } from '@tolgee/react';
import { isNullish } from 'remeda';

type Props = {
  value: string | null;
  onChange: (value: string | null) => void;
  disabled?: boolean;
};

/**
 * Single-select organisaatio picker at the koulutustoimija level. Uses the
 * shared organisaatiohierarkia fetch but stays decoupled from
 * `useCommonSearchParams`.
 */
export const OrganisaatioSingle = ({ value, onChange, disabled }: Props) => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';

  const { data } = useFetchOrganisaatiohierarkiat();
  const orgs: Array<OrganisaatioHierarkia> = getKoulutustoimijatToShow(
    data ?? null,
  );

  const options: SelectOption[] = orgs.map((org) => ({
    value: org.organisaatio_oid,
    label: org.organisaatio_nimi[locale]
      ? `${org.organisaatio_nimi[locale]}`
      : '',
  }));

  return (
    <ComboBox
      id="organisaatio-single"
      label={t('raportti.organisaatio')}
      value={disabled ? undefined : (value ?? undefined)}
      options={disabled ? [] : options}
      onChange={(_, selected) =>
        onChange(isNullish(selected) ? null : selected.value)
      }
    />
  );
};
