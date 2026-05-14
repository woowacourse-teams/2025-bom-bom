import { useCallback, useEffect, useRef } from 'react';

export function useDebounce<T extends unknown[]>(
  callback: (...args: T) => void,
  wait: number,
) {
  const timerIdRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const callbackRef = useRef(callback);

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
      if (timerIdRef.current) {
        clearTimeout(timerIdRef.current);
      }
    };
  }, []);

  return debounced;
}
