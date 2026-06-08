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

  const [selectedOppilaitos, setSelectedOppilaitos] =
    useQueryStateWithLocalStorage<string | null>('ovara_oppilaitos', {
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: null,
    });

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
    console.debug('EMPTY ALL OPISKELUOIKEUDEN TILA PARAMS');
    const keysToClear = ['ovara_opiskeluoikeuden_tila'];

    keysToClear.forEach((key) => localStorage.removeItem(key));

    setSelectedOppilaitos(null);
    setEtunimi(null);
    setSukunimi(null);
    setHetu(null);
    setOppijanumero(null);
    setSelectedOpiskeluoikeudenTila(null);
  };

  return {
    selectedOppilaitos,
    setSelectedOppilaitos,
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
