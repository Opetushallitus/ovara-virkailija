import { parseAsBoolean, useQueryState, UseQueryStateOptions } from 'nuqs';
import { useCallback, useEffect } from 'react';
import { useLocalStorage } from 'usehooks-ts';

//https://github.com/47ng/nuqs/discussions/606#discussioncomment-12343199
export const useQueryStateWithLocalStorage = <T>(
  key: string,
  options: UseQueryStateOptions<T> & {
    defaultValue: T;
  },
) => {
  // queryStateen defaultValue
  const [queryState, setQueryState] = useQueryState<T>(key, options);

  // localStoragessa oletuksena null
  const [localStorageState, setLocalStorageState] = useLocalStorage<
    ReturnType<UseQueryStateOptions<T>['parse']>
  >(key, queryState);

  useEffect(() => {
    // jos arvo on jo localStoragessa, ei tehdä mitään
    if (queryState === localStorageState) return;

    // queryState on default (tyhjä) ja localStorageStatessa arvo, asetetaan queryState localstorageen
    if (
      queryState === options.defaultValue &&
      localStorageState !== null &&
      localStorageState !== undefined
    ) {
      setQueryState(localStorageState);

      return;
    }
  }, [
    queryState,
    localStorageState,
    setLocalStorageState,
    setQueryState,
    options.defaultValue,
  ]);

  type Value = NonNullable<ReturnType<typeof options.parse>>;

  const setState = useCallback(
    (value: Value | null) => {
      setLocalStorageState(value);

      return setQueryState(value);
    },
    [setLocalStorageState, setQueryState],
  );

  return [queryState, setState] as const;
};

export const useBooleanQueryStateWithOptions = (
  key: string,
  defaultValue: boolean,
  clearOnDefault: boolean = false, // ei tyhjätä parametreista true/false default-arvoa
) => {
  const booleanOptions = parseAsBoolean.withOptions({ clearOnDefault });

  const [queryState, setQueryState] = useQueryStateWithLocalStorage<boolean>(
    key,
    {
      defaultValue,
      ...booleanOptions, // Spread booleanOptions to include parse, serialize, and eq
    },
  );

  useEffect(() => {
    // asetetaan true/false default-arvo searchParamsiin
    if (!window.location.search.includes(key)) {
      setQueryState(defaultValue);
    }
  }, [key, defaultValue, setQueryState]);

  return [queryState, setQueryState] as const;
};
