import fi from './messages/fi.json';

type Messages = typeof fi;

declare global {
  // Use type safe message keys with `next-intl`
  interface IntlMessages extends Messages {}
}
