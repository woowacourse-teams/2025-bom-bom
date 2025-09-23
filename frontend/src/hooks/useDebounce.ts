import { useCallback, useEffect, useRef } from 'react';

interface UseDebounceOptions {
  cancelOnUnmount?: boolean;
}

export function useDebounce<T extends unknown[]>(
  callback: (...args: T) => void,
  wait: number,
  options: UseDebounceOptions = {},
) {
  const timerIdRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const callbackRef = useRef(callback);
  const { cancelOnUnmount = true } = options;

  const debounced = useCallback(
    (...args: T) => {
      if (timerIdRef.current) {
        clearTimeout(timerIdRef.current);
      }

      timerIdRef.current = setTimeout(() => {
        callbackRef.current(...args);
      }, wait);
    },
    [wait],
  );

  useEffect(() => {
    callbackRef.current = callback;
  }, [callback]);

  useEffect(() => {
    return () => {
      if (!cancelOnUnmount) {
        return;
      }

      if (timerIdRef.current) {
        clearTimeout(timerIdRef.current);
      }
    };
  }, [cancelOnUnmount]);

  return debounced;
}
