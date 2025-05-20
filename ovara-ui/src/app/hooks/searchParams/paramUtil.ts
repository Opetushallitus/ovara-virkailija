import { parseAsBoolean } from 'nuqs';

export const createNullableBooleanOptions = (defaultValue: boolean | null) => ({
  ...parseAsBoolean,
  parse: (value: string | null) => (value === null ? null : value === 'true'),
  serialize: (value: boolean | null) => (value === null ? '' : String(value)),
  defaultValue,
  eq: (a: boolean | null, b: boolean | null) => a === b,
});
