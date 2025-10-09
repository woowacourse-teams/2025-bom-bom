import { useEffect } from 'react';
import { openExternalLink } from '@/utils/externalLink';
import type { RefObject } from 'react';

export const useExternalLinkHandler = (
  contentRef: RefObject<HTMLDivElement | null>,
) => {
  useEffect(() => {
    const contentEl = contentRef.current;
    if (!contentEl) return;

    const handleClick = (e: MouseEvent) => {
      const link = (e.target as HTMLElement).closest('a');
      if (link && link.href) {
        e.preventDefault();
        console.log(1);
        openExternalLink(link.href);
      }
    };

    contentEl.addEventListener('click', handleClick);
    return () => contentEl.removeEventListener('click', handleClick);
  }, [contentRef]);
};
