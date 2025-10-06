import { useEffect, useRef } from 'react';
import { useDebounce } from './useDebounce';
import { READ_THRESHOLD } from '@/constants/article';
import { getScrollPercent } from '@/utils/scroll';

interface UseScrollThresholdParams {
  enabled?: boolean;
  threshold?: number;
  throttleMs?: number;
  onTrigger: () => void;
}

export function useScrollThreshold({
  enabled = false,
  threshold = READ_THRESHOLD,
  throttleMs = 500,
  onTrigger,
}: UseScrollThresholdParams) {
  const startTimeRef = useRef(Date.now());

  const throttledHandleScroll = useDebounce(() => {
    if (!enabled) return;

    const scrollPercent = getScrollPercent();
    const durationMs = Date.now() - startTimeRef.current;

    if (scrollPercent >= threshold && durationMs >= throttleMs) {
      onTrigger();
    }
  }, 100);

  useEffect(() => {
    if (enabled && getScrollPercent() === 100) {
      onTrigger();
    }
  }, [enabled, onTrigger]);

  useEffect(() => {
    window.addEventListener('scroll', throttledHandleScroll);
    return () => window.removeEventListener('scroll', throttledHandleScroll);
  }, [throttledHandleScroll]);
}
