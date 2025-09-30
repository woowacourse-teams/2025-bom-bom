import { useCallback } from 'react';
import { addHighlightToDOM, removeHighlightFromDOM } from '../utils/highlight';
import { useLocalStorageState } from '@/hooks/useLocalStorageState';
import type { Highlight } from '../types/highlight';

type StorageKey = `guide-highlight-${string}`;

export const useLocalHighlightData = ({ articleId }: { articleId: number }) => {
  const storageKey: StorageKey = `guide-highlight-${articleId}`;
  const [highlights, setHighlights] = useLocalStorageState<
    Highlight[],
    typeof storageKey
  >(storageKey);

  const addHighlight = useCallback(
    (highlight: Omit<Highlight, 'id' | 'memo'>) => {
      const highlightWithMemo = { ...highlight, id: Date.now(), memo: '' };
      setHighlights((prev) => {
        if (!prev) {
          return [highlightWithMemo];
        }
        return [...prev, highlightWithMemo];
      });
      addHighlightToDOM(highlightWithMemo);
    },
    [setHighlights],
  );

  const removeHighlight = useCallback(
    ({ id }: { id: number }) => {
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
    isHighlightLoaded: !!highlights,
    addHighlight,
    removeHighlight,
    updateMemo,
  };
};
