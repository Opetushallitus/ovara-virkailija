import { useTranslate } from '@tolgee/react';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
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

const getOrganisaatioOptions = (
  locale: string,
  orgs: Array<OrganisaatioHierarkia>,
) => {
  if (isNullish(orgs)) {
    return [];
  } else {
    return orgs.map((org) => {
      return {
        value: org.organisaatio_oid,
        label: org.organisaatio_nimi[locale as LanguageCode]
          ? `${org.organisaatio_nimi[locale as LanguageCode]}`
          : '',
      };
    });
  }
};

export const OrganisaatioValikot = () => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';
  const organisaatiot = useFetchOrganisaatiohierarkiat().data;

  const {
    selectedKoulutustoimija,
    setSelectedKoulutustoimija,
    selectedOppilaitokset,
    setSelectedOppilaitokset,
    selectedToimipisteet,
    setSelectedToimipisteet,
  } = useCommonSearchParams();

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

  return (
    <Box>
      <ComboBox
        id={koulutustoimija_id}
        label={t(`raportti.${koulutustoimija_id}`)}
        value={selectedKoulutustoimija ?? ''}
        options={getOrganisaatioOptions(locale, koulutustoimijat)}
        onChange={changeKoulutustoimija}
      />
      <MultiComboBox
        id={oppilaitos_id}
        label={t(`raportti.${oppilaitos_id}`)}
        value={selectedOppilaitokset ?? []}
        options={getOrganisaatioOptions(locale, oppilaitokset)}
        onChange={changeOppilaitokset}
      />
      <MultiComboBox
        id={toimipiste_id}
        label={t(`raportti.${toimipiste_id}`)}
        value={selectedToimipisteet ?? []}
        options={getOrganisaatioOptions(locale, toimipisteet)}
        onChange={changeToimipisteet}
      />
    </Box>
  );
};

export const OppilaitosValikko = ({
  locale,
  organisaatiot,
  t,
}: {
  locale: string;
  organisaatiot: Array<OrganisaatioHierarkia> | null;
  t: (key: string) => string;
}) => {
  const {
    selectedOppilaitokset,
    setSelectedOppilaitokset,
    selectedKoulutustoimija,
  } = useCommonSearchParams();

  const oppilaitokset = getOppilaitoksetToShow(
    organisaatiot,
    selectedKoulutustoimija,
  );

  const changeOppilaitokset = (
    _: React.SyntheticEvent,
    value: Array<SelectOption>,
  ) => {
    return setSelectedOppilaitokset(
      isEmpty(value) ? null : value?.map((v) => v.value),
    );
  };

  const oppilaitos_id = 'oppilaitos';

  return (
    <MultiComboBox
      id={oppilaitos_id}
      label={t(`raportti.${oppilaitos_id}`)}
      value={selectedOppilaitokset ?? []}
      options={getOrganisaatioOptions(locale, oppilaitokset)}
      onChange={changeOppilaitokset}
    />
  );
};

export const ToimipisteValikko = ({
  locale,
  organisaatiot,
  t,
}: {
  locale: string;
  organisaatiot: Array<OrganisaatioHierarkia> | null;
  t: (key: string) => string;
}) => {
  const {
    selectedToimipisteet,
    setSelectedToimipisteet,
    selectedOppilaitokset,
    selectedKoulutustoimija,
  } = useCommonSearchParams();

  const toimipisteet = getToimipisteetToShow(
    organisaatiot,
    selectedOppilaitokset,
    selectedKoulutustoimija,
  );

  const changeToimipisteet = (
    _: React.SyntheticEvent,
    value: Array<SelectOption>,
  ) => {
    return setSelectedToimipisteet(
      isEmpty(value) ? null : value?.map((v) => v.value),
    );
  };

  const toimipiste_id = 'toimipiste';

  return (
    <MultiComboBox
      id={toimipiste_id}
      label={t(`raportti.${toimipiste_id}`)}
      value={selectedToimipisteet ?? []}
      options={getOrganisaatioOptions(locale, toimipisteet)}
      onChange={changeToimipisteet}
    />
  );
};
