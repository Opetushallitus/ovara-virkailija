import { useQueryState, UseQueryStateOptions } from 'nuqs';
import { useCallback, useEffect } from 'react';
import { useLocalStorage } from 'usehooks-ts';

//https://github.com/47ng/nuqs/discussions/606#discussioncomment-12343199
export const useQueryStateWithLocalStorage = <T>(
  key: string,
  options: UseQueryStateOptions<T> & {
    defaultValue: T;
  },
) => {
  // queryState is never null, it defaults to the defaultValue
  const [queryState, setQueryState] = useQueryState<T>(key, options);

  // localStorageState defaults to null
  const [localStorageState, setLocalStorageState] = useLocalStorage<
    ReturnType<UseQueryStateOptions<T>['parse']>
  >(key, queryState);

  useEffect(() => {
    // If queryState is the same as localStorageState, do nothing
    if (queryState === localStorageState) return;

    // If queryState is default (nothing there) and localStorageState is there, set localStorageState to queryState
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
