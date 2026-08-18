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

  const emptyAllKkHakijatTiedonsiirtoParams = () => {
    const keysToClear = [
      'ovara_kkts_haku',
      'ovara_kkts_hakukohde',
      'ovara_kkts_organisaatio',
      'ovara_kkts_valintarajaus',
    ];
    keysToClear.forEach((key) => localStorage.removeItem(key));
    setSelectedHaku(null);
    setSelectedHakukohde(null);
    setSelectedOrganisaatio(null);
    setSelectedValintarajaus(null);
  };

  return {
    selectedHaku,
    setSelectedHaku,
    selectedHakukohde,
    setSelectedHakukohde,
    selectedOrganisaatio,
    setSelectedOrganisaatio,
    selectedValintarajaus,
    setSelectedValintarajaus,
    emptyAllKkHakijatTiedonsiirtoParams,
  };
};
