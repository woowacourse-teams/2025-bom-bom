import { useCallback } from 'react';
import { HighlightType } from '../types/highlight';
import { useLocalStorageState } from '@/hooks/useLocalStorageState';

type StorageKeyType = `guide-highlight-${string}`;

export const useLocalHighlighData = ({ articleId }: { articleId: number }) => {
  const storageKey: StorageKeyType = `guide-highlight-${articleId}`;
  const [highlights, setHighlights] = useLocalStorageState<
    HighlightType[],
    typeof storageKey
  >(storageKey);

  const addHighlight = useCallback(
    (highlight: Omit<HighlightType, 'id' | 'memo'>) => {
      setHighlights((prev) => {
        if (!prev) {
          return [{ ...highlight, id: Date.now(), memo: '' }];
        }
        return [...prev, { ...highlight, id: Date.now(), memo: '' }];
      });
    },
    [setHighlights],
  );

  const removeHighlight = useCallback(
    (id: number) => {
      setHighlights((prev) => prev.filter((h) => h.id !== id));
    },
    [setHighlights],
  );

  const updateMemo = useCallback(
    (id: number, memo: string) => {
      setHighlights((prev) =>
        prev.map((highlight) =>
          highlight.id === id ? { ...highlight, memo } : highlight,
        ),
      );
    },
    [setHighlights],
  );

  return {
    highlights,
    addHighlight,
    removeHighlight,
    updateMemo,
  };
};
