'use client';
import { useQueryStateWithLocalStorage } from '@/app/hooks/searchParams/useQueryStateWithLocalStorage';

const parseNullableString = {
  parse: (value: string | null) => (value === null ? null : String(value)),
  defaultValue: null,
};

export const useKkHakijatTiedonsiirtoSearchParams = () => {
  const [selectedHaku, setSelectedHaku] = useQueryStateWithLocalStorage<
    string | null
  >('ovara_kkts_haku', parseNullableString);

  const [selectedHakukohde, setSelectedHakukohde] =
    useQueryStateWithLocalStorage<string | null>(
      'ovara_kkts_hakukohde',
      parseNullableString,
    );

  const [selectedHakukohderyhma, setSelectedHakukohderyhma] =
    useQueryStateWithLocalStorage<string | null>(
      'ovara_kkts_hakukohderyhma',
      parseNullableString,
    );

  const [selectedOrganisaatio, setSelectedOrganisaatio] =
    useQueryStateWithLocalStorage<string | null>(
      'ovara_kkts_organisaatio',
      parseNullableString,
    );

  const [selectedValintarajaus, setSelectedValintarajaus] =
    useQueryStateWithLocalStorage<string | null>(
      'ovara_kkts_valintarajaus',
      parseNullableString,
    );

  const [selectedOppijanumero, setSelectedOppijanumero] =
    useQueryStateWithLocalStorage<string | null>(
      'ovara_kkts_oppijanumero',
      parseNullableString,
    );

  const emptyAllKkHakijatTiedonsiirtoParams = () => {
    const keysToClear = [
      'ovara_kkts_haku',
      'ovara_kkts_hakukohde',
      'ovara_kkts_hakukohderyhma',
      'ovara_kkts_organisaatio',
      'ovara_kkts_valintarajaus',
      'ovara_kkts_oppijanumero',
    ];
    keysToClear.forEach((key) => localStorage.removeItem(key));
    setSelectedHaku(null);
    setSelectedHakukohde(null);
    setSelectedHakukohderyhma(null);
    setSelectedOrganisaatio(null);
    setSelectedValintarajaus(null);
    setSelectedOppijanumero(null);
  };

  return {
    selectedHaku,
    setSelectedHaku,
    selectedHakukohde,
    setSelectedHakukohde,
    selectedHakukohderyhma,
    setSelectedHakukohderyhma,
    selectedOrganisaatio,
    setSelectedOrganisaatio,
    selectedValintarajaus,
    setSelectedValintarajaus,
    selectedOppijanumero,
    setSelectedOppijanumero,
    emptyAllKkHakijatTiedonsiirtoParams,
  };
};
