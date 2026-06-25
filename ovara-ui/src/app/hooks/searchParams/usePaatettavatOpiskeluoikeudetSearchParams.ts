'use client';
import { useQueryStateWithLocalStorage } from './useQueryStateWithLocalStorage';

export const usePaatettavatOpiskeluoikeudetSearchParams = () => {
  const [selectedOpiskeluoikeudenTila, setSelectedOpiskeluoikeudenTila] =
    useQueryStateWithLocalStorage<string | null>(
      'ovara_opiskeluoikeuden_tila',
      {
        parse: (value) => (value === null ? null : String(value)),
        defaultValue: 'kaikki',
      },
    );

  const [etunimi, setEtunimi] = useQueryStateWithLocalStorage<string | null>(
    'ovara_etunimi',
    {
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: null,
    },
  );

  const [sukunimi, setSukunimi] = useQueryStateWithLocalStorage<string | null>(
    'ovara_sukunimi',
    {
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: null,
    },
  );

  const [hetu, setHetu] = useQueryStateWithLocalStorage<string | null>(
    'ovara_hetu',
    {
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: null,
    },
  );

  const [oppijanumero, setOppijanumero] = useQueryStateWithLocalStorage<
    string | null
  >('ovara_oppijanumero', {
    parse: (value) => (value === null ? null : String(value)),
    defaultValue: null,
  });

  const emptyAllPaatettavatOpiskeluoikeudetParams = () => {
    const keysToClear = [
      'ovara_opiskeluoikeuden_tila',
      'ovara_oppilaitos',
      'ovara_etunimi',
      'ovara_sukunimi',
      'ovara_hetu',
      'ovara_oppijanumero',
    ];

    keysToClear.forEach((key) => localStorage.removeItem(key));

    setSelectedOpiskeluoikeudenTila(null);
    setEtunimi(null);
    setSukunimi(null);
    setHetu(null);
    setOppijanumero(null);
  };

  return {
    etunimi,
    setEtunimi,
    sukunimi,
    setSukunimi,
    hetu,
    setHetu,
    oppijanumero,
    setOppijanumero,
    selectedOpiskeluoikeudenTila,
    setSelectedOpiskeluoikeudenTila,
    emptyAllPaatettavatOpiskeluoikeudetParams,
  };
};
