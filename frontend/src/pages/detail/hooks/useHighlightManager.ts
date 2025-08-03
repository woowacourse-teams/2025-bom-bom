import { useCallback, useEffect, useState } from 'react';
import { HighlightType } from '../types/highlight';
import { restoreHighlight } from '../utils/highlight';

export const useHighlightManager = () => {
  const [highlights, setHighlights] = useState<HighlightType[]>([]);

  const addHighlights = useCallback((highlight: HighlightType) => {
    setHighlights((prev) => [...prev, highlight]);
  }, []);

  useEffect(() => {
    if (highlights?.length === 0) return;

    highlights.forEach((h) => restoreHighlight(h));
  }, [highlights]);

  useEffect(() => {
    const handleMouseOver = (e: Event) => {
      const target = e.target as HTMLElement;
      if (target.tagName === 'MARK' && target.dataset.highlightId) {
        const id = target.dataset.highlightId;
        document
          .querySelectorAll(`mark[data-highlight-id="${id}"]`)
          .forEach((el) => el.classList.add('hovered-highlight'));
      }
    };

    const handleMouseOut = (e: Event) => {
      const target = e.target as HTMLElement;
      if (target.tagName === 'MARK' && target.dataset.highlightId) {
        const id = target.dataset.highlightId;
        document
          .querySelectorAll(`mark[data-highlight-id="${id}"]`)
          .forEach((el) => el.classList.remove('hovered-highlight'));
      }
    };

    document.addEventListener('mouseover', handleMouseOver);
    document.addEventListener('mouseout', handleMouseOut);

    return () => {
      document.removeEventListener('mouseover', handleMouseOver);
      document.removeEventListener('mouseout', handleMouseOut);
    };
  }, []);

  return {
    highlights,
    addHighlights,
  };
};
