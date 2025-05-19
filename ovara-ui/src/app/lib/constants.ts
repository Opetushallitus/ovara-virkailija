export const TOISEN_ASTEEN_RAPORTIT = [
  'koulutukset-toteutukset-hakukohteet',
  'hakijat',
  'hakeneet-hyvaksytyt-vastaanottaneet',
];

export const KK_RAPORTIT = [
  'kk-hakijat',
  'kk-hakeneet-hyvaksytyt-vastaanottaneet',
  'kk-koulutukset-toteutukset-hakukohteet',
];

export const RADIOGROUP_OPTIONS = ['neutral', 'yes', 'no'];
export const RADIOGROUP_BOOLEAN_OPTIONS = ['yes', 'no'];

export const TILAT = ['julkaistu', 'tallennettu', 'arkistoitu'];
export const SUKUPUOLET = ['neutral', '2', '1'];

export const KOULUTUSTOIMIJAORGANISAATIOTYYPPI = '01';
export const OPPILAITOSORGANISAATIOTYYPPI = '02';
export const TOIMIPISTEORGANISAATIOTYYPPI = '03';

export const DEFAULT_NUQS_OPTIONS = {
  clearOnDefault: false, // pidetään myös default-arvot url-parametreissä
} as const;
