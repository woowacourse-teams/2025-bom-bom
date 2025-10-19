import { useEffect, useState } from 'react';
import { isServer } from '@/utils/environment';

const MEDIA_FEATURE_KEYS = [
  'max-width',
  'min-width',
  'max-height',
  'min-height',
  'aspect-ratio',
  'min-aspect-ratio',
  'max-aspect-ratio',
  'min-resolution',
  'max-resolution',
  'orientation',
  'prefers-color-scheme',
  'hover',
  'pointer',
] as const;

type MediaFeatureKey = (typeof MEDIA_FEATURE_KEYS)[number];

function isMediaFeatureKey(key: unknown): key is MediaFeatureKey {
  return (
    typeof key === 'string' &&
    MEDIA_FEATURE_KEYS.includes(key as MediaFeatureKey)
  );
}

type PxNumber = number;
type RatioTuple = [number, number];

export type MediaCondition =
  | {
      key: 'max-width' | 'min-width' | 'max-height' | 'min-height';
      value: PxNumber;
    }
  | {
      key: 'aspect-ratio' | 'min-aspect-ratio' | 'max-aspect-ratio';
      value: RatioTuple;
    }
  | {
      key: 'min-resolution' | 'max-resolution';
      value: { amount: number; unit?: 'dppx' | 'dpi' };
    }
  | { key: 'orientation'; value: 'portrait' | 'landscape' }
  | { key: 'prefers-color-scheme'; value: 'dark' | 'light' }
  | { key: 'hover'; value: 'none' | 'hover' }
  | { key: 'pointer'; value: 'coarse' | 'fine' };

function convertToMediaQueryString(condition: MediaCondition): string {
  if (!('key' in condition) || !isMediaFeatureKey(condition.key)) {
    throw new Error('[useMediaQuery] 잘못된 condition 형식입니다.');
  }

  switch (condition.key) {
    case 'max-width':
    case 'min-width':
    case 'max-height':
    case 'min-height':
      return `(${condition.key}: ${condition.value}px)`;

    case 'aspect-ratio':
    case 'min-aspect-ratio':
    case 'max-aspect-ratio': {
      const [width, height] = condition.value;
      return `(${condition.key}: ${width}/${height})`;
    }

    case 'min-resolution':
    case 'max-resolution': {
      const unit = condition.value.unit ?? 'dppx';
      return `(${condition.key}: ${condition.value.amount}${unit})`;
    }

    case 'orientation':
    case 'prefers-color-scheme':
    case 'hover':
    case 'pointer':
      return `(${condition.key}: ${condition.value})`;
  }
}

const useMediaQuery = (condition: MediaCondition): boolean => {
  const getInitialMatches = () => {
    const query = convertToMediaQueryString(condition);
    return isServer ? false : window.matchMedia(query).matches;
  };

  const [matches, setMatches] = useState<boolean>(getInitialMatches);

  useEffect(() => {
    const query = convertToMediaQueryString(condition);
    const media = window.matchMedia(query);
    const listener = (e: MediaQueryListEvent) => setMatches(e.matches);
    media.addEventListener('change', listener);
    return () => media.removeEventListener('change', listener);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(condition)]);

  return matches;
};

export default useMediaQuery;
