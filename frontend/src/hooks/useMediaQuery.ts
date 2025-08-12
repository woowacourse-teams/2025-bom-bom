import { useEffect, useState } from 'react';

export type MediaFeatureKey =
  | 'max-width'
  | 'min-width'
  | 'max-height'
  | 'min-height'
  | 'aspect-ratio'
  | 'min-aspect-ratio'
  | 'max-aspect-ratio'
  | 'min-resolution'
  | 'max-resolution'
  | 'orientation'
  | 'prefers-color-scheme'
  | 'hover';

type PxNumber = number;
type RatioTuple = [number, number];
type ResolutionUnit = 'dppx' | 'dpi';

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
      value: { amount: number; unit?: ResolutionUnit };
    }
  | { key: 'orientation'; value: 'portrait' | 'landscape' }
  | { key: 'prefers-color-scheme'; value: 'dark' | 'light' }
  | { key: 'hover'; value: 'none' | 'hover' }
  | { key: 'pointer'; value: 'coarse' | 'fine' };

function toMediaString(condition: MediaCondition): string {
  if ('key' in condition === false || typeof condition.key !== 'string') {
    throw new Error('[useMediaQuery] 잘못된 KV 형식입니다.');
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
      const [w, h] = condition.value;
      return `(${condition.key}: ${w}/${h})`;
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

export function useMediaQuery(condition: MediaCondition): boolean;
export function useMediaQuery(condition: MediaCondition[]): boolean[];

/**
 * React 훅: `window.matchMedia`를 사용하여 특정 CSS 미디어 조건의 일치 여부를 감지하고,
 * 해당 조건이 변할 때마다 React 상태를 자동으로 업데이트합니다.
 *
 * ## 주요 특징
 * 1. **Key/Value 형식 제한**
 *    - 문자열 대신 `MediaCondition` 타입을 사용해 `(max-width: 768px)` 등 유효한 CSS 미디어 조건만 작성 가능.
 *    - `key`는 미리 정의된 `MediaFeatureKey` 중 하나여야 함.
 *    - `value`는 각 key에 맞는 타입(PxNumber, RatioTuple 등)만 허용.
 *
 * 2. **SSR 안전성**
 *    - 서버사이드 렌더링 환경에서는 `window`가 없으므로 기본값(`false` 또는 `false[]`) 반환.
 *
 * ## 매개변수
 * @param condition 감시할 미디어 조건 객체 또는 미디어 조건 객체 배열
 *
 * ## 반환값
 * - 단일 `MediaCondition`: 해당 조건이 현재 뷰포트에 일치하면 `true`, 아니면 `false`
 * - `MediaCondition[]`: 각 조건의 일치 여부를 같은 순서의 `boolean[]`로 반환
 *
 * ## 사용 예시
 * ```ts
 * // 단일 조건 사용
 * const isMobile = useMediaQuery({ key: 'max-width', value: 768 });
 * if (isMobile) {
 *   console.log('모바일 화면입니다');
 * }
 *
 * // 여러 조건 사용
 * const [isSmall, prefersDark] = useMediaQuery([
 *   { key: 'max-width', value: 640 },
 *   { key: 'prefers-color-scheme', value: 'dark' },
 * ]);
 * ```
 */
export function useMediaQuery(
  condition: MediaCondition | MediaCondition[],
): boolean | boolean[] {
  const toStrings = (condition: MediaCondition | MediaCondition[]) => {
    const list = Array.isArray(condition) ? condition : [condition];
    return list.map((item) =>
      typeof item === 'string' ? item : toMediaString(item),
    );
  };

  const getInitialMatches = () => {
    if (typeof window === 'undefined') {
      return Array.isArray(condition)
        ? Array<boolean>(condition.length).fill(false)
        : false;
    }

    const queries = toStrings(condition);
    return Array.isArray(condition)
      ? queries.map((query) => window.matchMedia(query).matches)
      : queries[0]
        ? window.matchMedia(queries[0]).matches
        : false;
  };

  const [matches, setMatches] = useState<boolean | boolean[]>(
    getInitialMatches,
  );

  useEffect(() => {
    const queries = toStrings(condition);

    if (Array.isArray(queries)) {
      const mediaList = queries.map((query) => window.matchMedia(query));
      const listener = () => setMatches(mediaList.map((m) => m.matches));
      mediaList.forEach((media) => media.addEventListener('change', listener));
      return () =>
        mediaList.forEach((media) =>
          media.removeEventListener('change', listener),
        );
    } else {
      const media = window.matchMedia(queries[0]);
      const listener = (e: MediaQueryListEvent) => setMatches(e.matches);
      media.addEventListener('change', listener);
      return () => media.removeEventListener('change', listener);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(condition)]);

  return matches;
}
