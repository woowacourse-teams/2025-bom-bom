import { useLocation } from '@tanstack/react-router';
import { useCallback, useEffect, useRef } from 'react';
import { useDebounce } from './useDebounce';
import { createStorage } from '@/utils/storage';

const DEFAULT_SCROLL_LOCATION = 0;

const useScrollRestoration = () => {
  const location = useLocation();
  const storageKey = `scroll-${location.pathname}`;
  const timerIdRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const scrollStorage = createStorage<number>(
    storageKey,
    DEFAULT_SCROLL_LOCATION,
  );

  const restoreScroll = useCallback((scrollLocation: number) => {
    if (timerIdRef.current) {
      clearTimeout(timerIdRef.current);
    }

    window.scroll({
      top: scrollLocation,
      behavior: 'smooth',
    });
  }, []);

  const handleScroll = useDebounce(() => {
    scrollStorage.set(window.scrollY);
  }, 100);

  useEffect(() => {
    const scrollLocation = scrollStorage.get();
    if (scrollLocation) {
      restoreScroll(scrollLocation);
    }
  }, [scrollStorage, restoreScroll]);

  useEffect(() => {
    window.addEventListener('scroll', handleScroll);

    return () => {
      window.removeEventListener('scroll', handleScroll);
    };
  }, [handleScroll]);
};

export default useScrollRestoration;
