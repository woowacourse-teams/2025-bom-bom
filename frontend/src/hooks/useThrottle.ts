import { useCallback, useEffect, useRef } from 'react';

export const useThrottle = (callback: () => void, wait: number) => {
  const lastExecutedRef = useRef(0);
  const callbackRef = useRef(callback);

  const throttled = useCallback(() => {
    const now = Date.now();
    if (now - lastExecutedRef.current >= wait) {
      callbackRef.current();
      lastExecutedRef.current = now;
    }
  }, [wait]);

  useEffect(() => {
    callbackRef.current = callback;
  }, [callback]);

  return throttled;
};
