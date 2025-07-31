import { useLocation } from '@tanstack/react-router';
import { useEffect } from 'react';
import useLocalStorage from './useLocalStorage';

const useScrollRestoration = () => {
  const location = useLocation();
  const storageKey = `scroll-${location.pathname}`;
  const { get: getScrollLocation, set: setScrollLocation } =
    useLocalStorage<number>(storageKey, 0);

  useEffect(() => {
    const scrollLocation = getScrollLocation();
    if (scrollLocation) {
      window.scrollTo(0, scrollLocation);
    }

    const handleScrollLocation = () => {
      setScrollLocation(window.scrollY);
    };
    window.addEventListener('beforeunload', handleScrollLocation);

    return () => {
      setScrollLocation(window.scrollY);
      window.removeEventListener('beforeunload', handleScrollLocation);
    };
  }, [location.pathname, getScrollLocation, setScrollLocation]);
};

export default useScrollRestoration;
