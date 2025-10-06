import { useCallback, useEffect, useRef } from 'react';

interface UseDebounceOptions {
  autoCancelOnUnmount?: boolean;
}

export function useDebounce<T extends unknown[]>(
  callback: (...args: T) => void,
  debounceMs: number,
  options: UseDebounceOptions = {},
) {
  const timerIdRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const callbackRef = useRef(callback);
  const { autoCancelOnUnmount = true } = options;

  const debounced = useCallback(
    (...args: T) => {
      if (timerIdRef.current) {
        clearTimeout(timerIdRef.current);
      }

      timerIdRef.current = setTimeout(() => {
        callbackRef.current(...args);
      }, debounceMs);
    },
    [debounceMs],
  );

  useEffect(() => {
    callbackRef.current = callback;
  }, [callback]);

  useEffect(() => {
    return () => {
      if (!autoCancelOnUnmount) {
        return;
      }

      if (timerIdRef.current) {
        clearTimeout(timerIdRef.current);
      }
    };
  }, [autoCancelOnUnmount]);

  return debounced;
}
