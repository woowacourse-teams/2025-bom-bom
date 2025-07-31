import { useLocation } from '@tanstack/react-router';
import { useCallback, useLayoutEffect, useRef } from 'react';
import useLocalStorage from './useLocalStorage';

const useScrollRestoration = (path: string) => {
  const location = useLocation();
  const storageKey = `scroll-${location.pathname}`;
  const { data: scrollLocation, set: setScrollLocation } =
    useLocalStorage<number>(storageKey, 0);
  const timerIdRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const restoreScroll = useCallback((scrollLocation: number) => {
    if (timerIdRef.current) {
      clearTimeout(timerIdRef.current);
    }

    timerIdRef.current = setTimeout(() => {
      window.scroll({
        top: scrollLocation,
        behavior: 'smooth',
      });
    }, 400);
  }, []);

  useLayoutEffect(() => {
    if (path !== location.pathname) return;

    if (scrollLocation) {
      restoreScroll(scrollLocation);
    }

    const saveLocation = () => {
      setScrollLocation(window.scrollY);
    };
    window.addEventListener('visibilitychange', saveLocation);

    return () => {
      setScrollLocation(window.scrollY);
      window.removeEventListener('visibilitychange', saveLocation);
    };
  }, [
    path,
    location.pathname,
    scrollLocation,
    restoreScroll,
    setScrollLocation,
  ]);
};

export default useScrollRestoration;
