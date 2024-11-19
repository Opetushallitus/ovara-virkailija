export type LanguageCode = 'fi' | 'sv' | 'en';

export type Kielistetty<T = string> = Partial<Record<LanguageCode, T>>;

export type User = {
  userOid: string;
  authorities: Array<string>;
  asiointikieli: string;
};
