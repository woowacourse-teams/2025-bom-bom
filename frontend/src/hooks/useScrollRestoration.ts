import { useLocation } from '@tanstack/react-router';
import { useCallback, useEffect, useRef } from 'react';
import { useDebounce } from './useDebounce';
import useLocalStorage from './useLocalStorage';

const useScrollRestoration = () => {
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
    }, 300);
  }, []);

  const handleScroll = useDebounce(() => {
    setScrollLocation(window.scrollY);
  }, 100);

  useEffect(() => {
    if (scrollLocation) {
      restoreScroll(scrollLocation);
    }
  }, [restoreScroll, scrollLocation]);

  useEffect(() => {
    window.addEventListener('scroll', handleScroll);

    return () => {
      window.removeEventListener('scroll', handleScroll);
    };
  }, [handleScroll]);
};

export default useScrollRestoration;
