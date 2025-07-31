import { useEffect, useRef } from 'react';
import { useThrottle } from './useThrottle';
import { getScrollPercent } from '@/utils/scroll';

interface UseScrollThresholdParams {
  enabled?: boolean;
  threshold?: number;
  throttleMs?: number;
  onTrigger: () => void;
}

export function useScrollThreshold({
  enabled = false,
  threshold = 70,
  throttleMs = 500,
  onTrigger,
}: UseScrollThresholdParams) {
  const startTimeRef = useRef(Date.now());

  const throttledHandleScroll = useThrottle(() => {
    if (!enabled) return;

    const scrollPercent = getScrollPercent();
    const elapsed = Date.now() - startTimeRef.current;

    if (scrollPercent >= threshold && elapsed >= throttleMs) {
      onTrigger();
    }
  }, 500);

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
