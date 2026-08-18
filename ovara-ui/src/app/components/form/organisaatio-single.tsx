import { ComboBox, SelectOption } from '@/app/components/form/multicombobox';
import { useFetchOrganisaatiohierarkiat } from '@/app/hooks/useFetchOrganisaatiohierarkiat';
import { useAuthorizedUser } from '@/app/components/providers/authorized-user-provider';
import { LanguageCode, OrganisaatioHierarkia } from '@/app/lib/types/common';
import { getKaikkiOrganisaatiotToShow } from '@/app/lib/utils';
import {
  KOULUTUSTOIMIJAORGANISAATIOTYYPPI,
  OPPILAITOSORGANISAATIOTYYPPI,
  TOIMIPISTEORGANISAATIOTYYPPI,
} from '@/app/lib/constants';
import { useTranslate } from '@tolgee/react';
import { isNullish } from 'remeda';

const ORGANISAATIOTYYPPI_ORDER = [
  KOULUTUSTOIMIJAORGANISAATIOTYYPPI,
  OPPILAITOSORGANISAATIOTYYPPI,
  TOIMIPISTEORGANISAATIOTYYPPI,
];

const getTasoIndex = (org: OrganisaatioHierarkia) => {
  const index = ORGANISAATIOTYYPPI_ORDER.findIndex((tyyppi) =>
    org.organisaatiotyypit.includes(tyyppi),
  );
  return index === -1 ? ORGANISAATIOTYYPPI_ORDER.length : index;
};

type Props = {
  value: string | null;
  onChange: (value: string | null) => void;
  disabled?: boolean;
  required?: boolean;
};

/**
 * Yhden organisaation valitsin. Tarjoaa kaikki organisaatiotasot joihin
 * käyttäjällä on oikeus (koulutustoimijat, oppilaitokset, toimipisteet), jotta
 * myös koulutustoimijaa alemmalla tasolla oikeutensa saanut käyttäjä näkee
 * valittavia organisaatioita. Käyttää jaettua organisaatiohierarkiahakua mutta
 * pysyy irrallaan `useCommonSearchParams`-tilasta.
 */
export const OrganisaatioSingle = ({
  value,
  onChange,
  disabled,
  required,
}: Props) => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';

  const { data } = useFetchOrganisaatiohierarkiat();
  const orgs: Array<OrganisaatioHierarkia> = getKaikkiOrganisaatiotToShow(
    data ?? null,
  );

  // Järjestetään ensin tasoittain (koulutustoimija -> oppilaitos -> toimipiste)
  // ja tason sisällä nimen mukaan. sortOptions=false säilyttää tämän
  // järjestyksen ComboBoxissa.
  const collator = new Intl.Collator(locale);
  const options: SelectOption[] = orgs
    .map((org) => ({
      tasoIndex: getTasoIndex(org),
      value: org.organisaatio_oid,
      label: org.organisaatio_nimi[locale]
        ? `${org.organisaatio_nimi[locale]}`
        : '',
    }))
    .sort(
      (a, b) => a.tasoIndex - b.tasoIndex || collator.compare(a.label, b.label),
    )
    .map(({ value, label }) => ({ value, label }));

  return (
    <ComboBox
      id="organisaatio-single"
      label={t('raportti.organisaatio')}
      value={disabled ? undefined : (value ?? undefined)}
      options={disabled ? [] : options}
      sortOptions={false}
      required={required}
      onChange={(_, selected) =>
        onChange(isNullish(selected) ? null : selected.value)
      }
    />
  );
};
