export type LanguageCode = 'fi' | 'sv' | 'en';

export type Kielistetty<T = string> = Partial<Record<LanguageCode, T>>;

export type User = {
  userOid: string;
  authorities: Array<string>;
  asiointikieli: string;
};

type Organisaatio = {
  organisaatio_oid: string;
  organisaatio_nimi: Kielistetty;
  organisaatiotyyppi: string;
};

type OrganisaatioHierarkia = {
  organisaatio_oid: string;
  organisaatio_nimi: Kielistetty;
  organisaatiotyypit: Array<string>;
  oppilaitostyyppi: string | null;
  tila: string;
  parent_oids: Array<string>;
  children: Array<OrganisaatioHierarkia>;
};
