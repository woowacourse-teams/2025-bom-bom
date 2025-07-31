import { useEffect } from 'react';
import { HighlightType } from '../types/highlight';
import { restoreHighlight } from '../utils/highlight';

export const useHighlightManager = (highlights: HighlightType[]) => {
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

    const handleClick = (e: Event) => {
      const target = e.target as HTMLElement;
      if (target.tagName === 'MARK' && target.dataset.highlightId) {
        console.log('CCCCCCClick'); // → openFloatingToolbar(target) 같은 UI 호출 가능
      }
    };

    document.addEventListener('mouseover', handleMouseOver);
    document.addEventListener('mouseout', handleMouseOut);
    document.addEventListener('click', handleClick);

    return () => {
      document.removeEventListener('mouseover', handleMouseOver);
      document.removeEventListener('mouseout', handleMouseOut);
      document.removeEventListener('click', handleClick);
    };
  }, []);
};
