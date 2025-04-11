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
  const organisaatiot = useFetchOrganisaatiohierarkiat().data;

  return (
    <Box>
      <Koulutustoimija organisaatiot={organisaatiot} />
      <Oppilaitos organisaatiot={organisaatiot} />
      <Toimipiste organisaatiot={organisaatiot} />
    </Box>
  );
};

export const Koulutustoimija = ({
  organisaatiot,
}: {
  organisaatiot: Array<OrganisaatioHierarkia> | null;
}) => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';

  const {
    selectedKoulutustoimija,
    setSelectedKoulutustoimija,
    setSelectedOppilaitokset,
    setSelectedToimipisteet,
  } = useCommonSearchParams();

  const koulutustoimija_id = 'koulutustoimija';

  const koulutustoimijat = getKoulutustoimijatToShow(organisaatiot);

  const changeKoulutustoimija = (
    _: React.SyntheticEvent,
    value: SelectOption | null,
  ) => {
    setSelectedKoulutustoimija(isNullish(value) ? null : value?.value);
    // tyhjätään oppilaitos ja toimipisteet
    setSelectedOppilaitokset(null);
    setSelectedToimipisteet(null);
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
    </Box>
  );
};

export const Oppilaitos = ({
  organisaatiot,
}: {
  organisaatiot: Array<OrganisaatioHierarkia> | null;
}) => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';

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

export const Toimipiste = ({
  organisaatiot,
}: {
  organisaatiot: Array<OrganisaatioHierarkia> | null;
}) => {
  const { t } = useTranslate();
  const user = useAuthorizedUser();
  const locale = (user?.asiointikieli as LanguageCode) ?? 'fi';

  const {
    selectedToimipisteet,
    setSelectedToimipisteet,
    selectedOppilaitokset,
    selectedKoulutustoimija,
  } = useCommonSearchParams();

  const toimipisteet = getToimipisteetToShow(
    organisaatiot,
    selectedToimipisteet,
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
