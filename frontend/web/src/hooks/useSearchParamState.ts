import { useNavigate, useSearch } from '@tanstack/react-router';
import { useCallback, useMemo } from 'react';

type SearchParamValue = string | number | boolean | null;

export function useSearchParamState<T extends SearchParamValue>(
  key: string,
  options: {
    defaultValue: T;
    replace?: boolean;
  },
): [T, (newValue: T | ((prev: T) => T)) => void];

export function useSearchParamState<T extends SearchParamValue = string>(
  key: string,
  options?: {
    defaultValue?: undefined;
    replace?: boolean;
  },
): [T | null, (newValue: T | null | ((prev: T | null) => T | null)) => void];

export function useSearchParamState<T extends SearchParamValue = string>(
  key: string,
  options?: {
    defaultValue?: T;
    replace?: boolean;
  },
) {
  const navigate = useNavigate();
  const search = useSearch({ strict: false }) as Record<string, unknown>;

  const { defaultValue = null, replace = true } = options || {};

  const value = useMemo(() => {
    const raw = search[key] ?? defaultValue;
    return raw as T | null;
  }, [search, key, defaultValue]);

  const setValue = useCallback(
    (newValue: (T | null) | ((prev: T | null) => T | null)) => {
      const resolved =
        typeof newValue === 'function'
          ? (newValue as (prev: T | null) => T | null)(value)
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
    [key, navigate, replace, value],
  );

  return [value, setValue] as const;
}
