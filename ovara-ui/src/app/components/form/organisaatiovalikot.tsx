import { useTranslations } from 'next-intl';
import { useSearchParams } from '@/app/hooks/useSearchParams';
import { Box } from '@mui/material';
import { isEmpty, isNullish } from 'remeda';
import { useFetchOrganisaatiohierarkiat } from '@/app/hooks/useFetchOrganisaatiohierarkiat';
import { useAuthorizedUser } from '@/app/contexts/AuthorizedUserProvider';
import { LanguageCode, OrganisaatioHierarkia } from '@/app/lib/types/common';
import {
  ComboBox,
  MultiComboBox,
  SelectOption,
} from '@/app/components/form/multicombobox';
import {
  getKoulutustoimijatToShow,
  getOppilaitoksetToShow,
  getToimipisteetToShow,
} from '@/app/lib/utils';

export const OrganisaatioValikot = () => {
  const t = useTranslations();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';
  const organisaatiot = useFetchOrganisaatiohierarkiat();

  const {
    selectedKoulutustoimija,
    setSelectedKoulutustoimija,
    selectedOppilaitokset,
    setSelectedOppilaitokset,
    selectedToimipisteet,
    setSelectedToimipisteet,
  } = useSearchParams();

  const koulutustoimija_id = 'koulutustoimija';
  const oppilaitos_id = 'oppilaitos';
  const toimipiste_id = 'toimipiste';

  const koulutustoimijat = getKoulutustoimijatToShow(organisaatiot);

  const oppilaitokset = getOppilaitoksetToShow(
    organisaatiot,
    selectedKoulutustoimija,
  );

  const toimipisteet = getToimipisteetToShow(
    organisaatiot,
    selectedOppilaitokset,
    selectedKoulutustoimija,
  );

  const changeKoulutustoimija = (
    _: React.SyntheticEvent,
    value: SelectOption | null,
  ) => {
    setSelectedKoulutustoimija(isNullish(value) ? null : value?.value);
  };

  const changeOppilaitokset = (
    _: React.SyntheticEvent,
    value: Array<SelectOption>,
  ) => {
    return setSelectedOppilaitokset(
      isEmpty(value) ? null : value?.map((v) => v.value),
    );
  };

  const changeToimipisteet = (
    _: React.SyntheticEvent,
    value: Array<SelectOption>,
  ) => {
    return setSelectedToimipisteet(
      isEmpty(value) ? null : value?.map((v) => v.value),
    );
  };

  const getOrganisaatioOptions = (orgs: Array<OrganisaatioHierarkia>) => {
    if (isNullish(orgs)) {
      return [];
    } else {
      return orgs.map((org) => {
        return {
          value: org.organisaatio_oid,
          // TODO: Jos org tila jätetään labeliin, täytyy lisätä käännös
          label: `${org.organisaatio_nimi[locale]} (${org.tila})` || '',
        };
      });
    }
  };

  return (
    <Box>
      <ComboBox
        id={koulutustoimija_id}
        label={t(`raportti.${koulutustoimija_id}`)}
        value={selectedKoulutustoimija ?? ''}
        options={getOrganisaatioOptions(koulutustoimijat)}
        onChange={changeKoulutustoimija}
      />
      <MultiComboBox
        id={oppilaitos_id}
        label={t(`raportti.${oppilaitos_id}`)}
        value={selectedOppilaitokset ?? []}
        options={getOrganisaatioOptions(oppilaitokset)}
        onChange={changeOppilaitokset}
      />
      <MultiComboBox
        id={toimipiste_id}
        label={t(`raportti.${toimipiste_id}`)}
        value={selectedToimipisteet ?? []}
        options={getOrganisaatioOptions(toimipisteet)}
        onChange={changeToimipisteet}
      />
    </Box>
  );
};
