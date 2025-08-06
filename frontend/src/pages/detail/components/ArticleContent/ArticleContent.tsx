import styled from '@emotion/styled';
import { useEffect, useRef } from 'react';
import { useHighlightHoverEffect } from '../../hooks/useHighlightHoverEffect';
import { HighlightType } from '../../types/highlight';
import { restoreHighlight } from '../../utils/highlight';
import FloatingToolbar from '../FloatingToolbar/FloatingToolbar';
import { FloatingToolbarMode } from '../FloatingToolbar/FloatingToolbar.types';
import { components } from '@/types/openapi';

interface ArticleContentProps {
  articleContent: components['schemas']['ArticleDetailResponse']['contents'];
  highlights: HighlightType[] | null | undefined;
  onHighlightClick: ({
    mode,
    selection,
    highlightId,
  }: {
    mode: FloatingToolbarMode;
    selection: Selection | null;
    highlightId: number | null;
  }) => void;
  onMemoClick: ({
    mode,
    selection,
  }: {
    mode: FloatingToolbarMode;
    selection: Selection | null;
  }) => void;
}

const ArticleContent = ({
  articleContent,
  highlights,
  onHighlightClick,
  onMemoClick,
}: ArticleContentProps) => {
  const contentRef = useRef<HTMLDivElement>(null);

  useHighlightHoverEffect();

  useEffect(() => {
    if (!highlights || highlights?.length === 0 || !articleContent) return;

    highlights.forEach((highlight) => restoreHighlight(highlight));
  }, [articleContent, highlights]);

  return (
    <>
      <Container
        ref={contentRef}
        dangerouslySetInnerHTML={{ __html: articleContent ?? '' }}
      />
      <FloatingToolbar
        selectionTargetRef={contentRef}
        onHighlightClick={onHighlightClick}
        onMemoClick={onMemoClick}
      />
    </>
  );
};

export default ArticleContent;

const Container = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;

  mark[data-highlight-id] {
    background-color: #ffeb3b;
    transition: box-shadow 0.2s ease-in-out;
  }

  mark[data-highlight-id].hovered-highlight {
    box-shadow: 0 0 6px rgb(0 0 0 / 30%);
    cursor: pointer;
  }
`;
