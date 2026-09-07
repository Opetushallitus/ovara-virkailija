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

  const [selectedOppilaitos, setSelectedOppilaitos] =
    useQueryStateWithLocalStorage<string | null>(
      'ovara_ta_oppilaitos',
      parseNullableString,
    );

  const [selectedToimipiste, setSelectedToimipiste] =
    useQueryStateWithLocalStorage<string | null>(
      'ovara_ta_toimipiste',
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
      'ovara_ta_oppilaitos',
      'ovara_ta_toimipiste',
      'ovara_ta_valintarajaus',
    ];
    keysToClear.forEach((key) => localStorage.removeItem(key));
    setSelectedHaku(null);
    setSelectedHakukohde(null);
    setSelectedOppilaitos(null);
    setSelectedToimipiste(null);
    setSelectedValintarajaus(null);
  };

  return {
    selectedHaku,
    setSelectedHaku,
    selectedHakukohde,
    setSelectedHakukohde,
    selectedOppilaitos,
    setSelectedOppilaitos,
    selectedToimipiste,
    setSelectedToimipiste,
    selectedValintarajaus,
    setSelectedValintarajaus,
    emptyAllToisenasteenHakijatParams,
  };
};
