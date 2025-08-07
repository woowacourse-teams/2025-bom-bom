import { useCallback, useEffect, useRef } from 'react';
import { useDebounce } from './useDebounce';
import { READ_THRESHOLD } from '@/constants/article';
import { createStorage } from '@/utils/localStorage';
import { getScrollPercent } from '@/utils/scroll';

const DEFAULT_SCROLL_LOCATION = 0;

type StorageKeyType = `scroll-${string}`;

interface UseScrollRestorationParams {
  pathname: string;
  threshold?: number;
}

const useScrollRestoration = ({
  pathname,
  threshold = READ_THRESHOLD,
}: UseScrollRestorationParams) => {
  const storageKey: StorageKeyType = `scroll-${pathname}`;
  const timerIdRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const scrollStorage = createStorage<number, StorageKeyType>(
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
    const scrollPercent = getScrollPercent();
    if (scrollPercent >= threshold) {
      scrollStorage.remove();
      return;
    }

    scrollStorage.set(window.scrollY);
  }, 100);

  useEffect(() => {
    const scrollLocation = scrollStorage.get();
    if (scrollLocation) {
      restoreScroll(scrollLocation);
    }
    // store 생성될 때마다 다시 복구하는 이슈
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [restoreScroll]);

  useEffect(() => {
    window.addEventListener('scroll', handleScroll);

    return () => {
      window.removeEventListener('scroll', handleScroll);
    };
  }, [handleScroll]);
};

export default useScrollRestoration;
