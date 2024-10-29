export type LanguageCode = 'fi' | 'sv' | 'en';

export type Kielistetty<T = string> = Partial<Record<LanguageCode, T>>;
