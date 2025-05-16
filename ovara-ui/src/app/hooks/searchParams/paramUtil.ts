import { DEFAULT_NUQS_OPTIONS } from '@/app/lib/constants';
import { parseAsBoolean } from 'nuqs';

export const createBooleanOptions = (defaultValue: boolean) => ({
  ...parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS),
  defaultValue,
  eq: (a: boolean, b: boolean) => a === b,
});

export const createNullableBooleanOptions = (defaultValue: boolean | null) => ({
  ...parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS),
  parse: (value: string | null) => (value === null ? null : value === 'true'),
  serialize: (value: boolean | null) => (value === null ? '' : String(value)),
  defaultValue,
  eq: (a: boolean | null, b: boolean | null) => a === b,
});
