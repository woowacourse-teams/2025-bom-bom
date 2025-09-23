import { useCallback } from 'react';
import { Highlight } from '../types/highlight';
import { removeHighlightFromDOM } from '../utils/highlight';
import { useLocalStorageState } from '@/hooks/useLocalStorageState';

type StorageKey = `guide-highlight-${string}`;

export const useLocalHighlightData = ({ articleId }: { articleId: number }) => {
  const storageKey: StorageKey = `guide-highlight-${articleId}`;
  const [highlights, setHighlights] = useLocalStorageState<
    Highlight[],
    typeof storageKey
  >(storageKey);

  const addHighlight = useCallback(
    (highlight: Omit<Highlight, 'id' | 'memo'>) => {
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
      removeHighlightFromDOM(id);
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
