import { useLocation } from '@tanstack/react-router';
import { useLayoutEffect } from 'react';
import { useDebounce } from './useDebounce';
import useLocalStorage from './useLocalStorage';

const useScrollRestoration = (path: string) => {
  const location = useLocation();
  const storageKey = `scroll-${location.pathname}`;
  const { get: getScrollLocation, set: setScrollLocation } =
    useLocalStorage<number>(storageKey, 0);

  const restoreScroll = useDebounce((scrollLocation: number) => {
    window.scroll({
      top: scrollLocation,
      behavior: 'smooth',
    });
  }, 400);

  useLayoutEffect(() => {
    if (path !== location.pathname) return;

    const scrollLocation = getScrollLocation();
    if (scrollLocation) {
      restoreScroll(scrollLocation);
    }

    const saveLocation = () => {
      setScrollLocation(window.scrollY);
    };
    window.addEventListener('beforeunload', saveLocation);

    return () => {
      setScrollLocation(window.scrollY);
      window.removeEventListener('beforeunload', saveLocation);
    };
  }, [restoreScroll, getScrollLocation, setScrollLocation]);
};

export default useScrollRestoration;
