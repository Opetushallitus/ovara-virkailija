'use client';
import { useQueryStateWithLocalStorage } from '@/app/hooks/searchParams/useQueryStateWithLocalStorage';

const parseNullableString = {
  parse: (value: string | null) => (value === null ? null : String(value)),
  defaultValue: null,
};

export const useToisenasteenHakijatSearchParams = () => {
  const [selectedHaku, setSelectedHaku] = useQueryStateWithLocalStorage<
    string | null
  >('ovara_ta_haku', parseNullableString);

  const [selectedHakukohde, setSelectedHakukohde] =
    useQueryStateWithLocalStorage<string | null>(
      'ovara_ta_hakukohde',
      parseNullableString,
    );

  const [selectedOrganisaatio, setSelectedOrganisaatio] =
    useQueryStateWithLocalStorage<string | null>(
      'ovara_ta_organisaatio',
      parseNullableString,
    );

  const [selectedValintarajaus, setSelectedValintarajaus] =
    useQueryStateWithLocalStorage<string | null>(
      'ovara_ta_valintarajaus',
      parseNullableString,
    );

  const emptyAllToisenasteenHakijatParams = () => {
    const keysToClear = [
      'ovara_ta_haku',
      'ovara_ta_hakukohde',
      'ovara_ta_organisaatio',
      'ovara_ta_valintarajaus',
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
    emptyAllToisenasteenHakijatParams,
  };
};
