import { useEffect, useRef } from 'react';
import { useThrottle } from './useThrottle';
import { getScrollPercent } from '@/utils/scroll';

interface UseScrollTriggerParams {
  enabled?: boolean;
  threshold?: number;
  delay?: number;
  onTrigger: () => void;
}

export function useScrollTrigger({
  enabled = true,
  threshold = 70,
  delay = 500,
  onTrigger,
}: UseScrollTriggerParams) {
  const startTimeRef = useRef(Date.now());

  const throttledHandleScroll = useThrottle(() => {
    if (!enabled) return;

    const scrollPercent = getScrollPercent();
    const elapsed = Date.now() - startTimeRef.current;

    if (scrollPercent >= threshold && elapsed >= delay) {
      onTrigger();
    }
  }, 500);

  useEffect(() => {
    window.addEventListener('scroll', throttledHandleScroll);
    return () => window.removeEventListener('scroll', throttledHandleScroll);
  }, [throttledHandleScroll]);
}
