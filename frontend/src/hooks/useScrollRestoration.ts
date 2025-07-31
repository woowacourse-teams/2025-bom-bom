import { useLocation } from '@tanstack/react-router';
import { useLayoutEffect } from 'react';
import useLocalStorage from './useLocalStorage';

const useScrollRestoration = (path: string) => {
  const location = useLocation();
  const storageKey = `scroll-${location.pathname}`;
  const { get: getScrollLocation, set: setScrollLocation } =
    useLocalStorage<number>(storageKey, 0);

  useLayoutEffect(() => {
    if (path !== location.pathname) return;

    const scrollLocation = getScrollLocation();
    if (scrollLocation) {
      setTimeout(() => {
        window.scroll({
          top: scrollLocation,
          behavior: 'smooth',
        });
      }, 400);
    }

    const handleScrollLocation = () => {
      setScrollLocation(window.scrollY);
    };
    window.addEventListener('beforeunload', handleScrollLocation);

    return () => {
      setScrollLocation(window.scrollY);
      window.removeEventListener('beforeunload', handleScrollLocation);
    };
  }, [getScrollLocation, setScrollLocation]);
};

export default useScrollRestoration;
