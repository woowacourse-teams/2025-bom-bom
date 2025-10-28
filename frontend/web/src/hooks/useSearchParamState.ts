import { useNavigate, useSearch } from '@tanstack/react-router';
import { useCallback, useMemo } from 'react';

type SearchParamValue = string | number | boolean | null;

export function useSearchParamState<T extends SearchParamValue = string>(
  key: string,
  options?: {
    defaultValue?: T;
    replace?: boolean;
  },
) {
  const navigate = useNavigate();
  const search = useSearch({ strict: false }) as Record<string, unknown>;

  const { defaultValue, replace = true } = options || {};

  const value = useMemo(() => {
    const raw = search[key];
    return raw as T;
  }, [search, key]);

  const setValue = useCallback(
    (newValue: T | ((prev: T) => T)) => {
      const resolved =
        typeof newValue === 'function'
          ? (newValue as (prev: T) => T)(value ?? defaultValue!)
          : newValue;

      navigate({
        search: (prev) => {
          const next = { ...(prev as Record<string, unknown>) };

          if (resolved === null || resolved === undefined) {
            delete next[key];
          } else {
            next[key] = resolved;
          }

          return next as never;
        },
        replace,
        resetScroll: false,
      });
    },
    [key, navigate, replace, value, defaultValue],
  );

  return [value ?? defaultValue, setValue] as const;
}
