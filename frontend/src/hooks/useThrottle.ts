import { useCallback, useEffect, useRef } from 'react';

export const useThrottle = <T extends any[]>(
  callback: (...args: T) => void,
  wait: number,
) => {
  const lastExecutedRef = useRef(0);
  const callbackRef = useRef(callback);

  const throttled = useCallback(
    (...args: T) => {
      const now = Date.now();
      if (now - lastExecutedRef.current >= wait) {
        callbackRef.current(...args);
        lastExecutedRef.current = now;
      }
    },
    [wait],
  );

  useEffect(() => {
    callbackRef.current = callback;
  }, [callback]);

  return throttled;
};
