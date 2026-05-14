import { useCallback, useEffect, useRef, useState } from 'react';

interface UseToastParams {
  duration: number;
}

const useToast = ({ duration }: UseToastParams) => {
  const [isVisible, setIsVisible] = useState(false);
  const timerIdRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const showToast = useCallback(
    (message: string) => {
      if (message.length <= 0) return;
      if (timerIdRef.current) {
        clearTimeout(timerIdRef.current);
      }

      setIsVisible(true);

      timerIdRef.current = setTimeout(() => {
        setIsVisible(false);
      }, duration);
    },
    [duration],
  );

  useEffect(() => {
    return () => {
      if (timerIdRef.current) {
        clearTimeout(timerIdRef.current);
      }
    };
  }, []);

  return {
    showToast,
    isVisible,
  };
};

export default useToast;
