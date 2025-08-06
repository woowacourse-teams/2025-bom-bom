import styled from '@emotion/styled';
import { useEffect, useRef } from 'react';
import { processContent } from './ArticleContent.utils';
import { useHighlightHoverEffect } from '../../hooks/useHighlightHoverEffect';
import { HighlightType } from '../../types/highlight';
import { restoreHighlight } from '../../utils/highlight';
import FloatingToolbar from '../FloatingToolbar/FloatingToolbar';
import { components } from '@/types/openapi';

interface ArticleContentProps {
  articleContent: components['schemas']['ArticleDetailResponse']['contents'];
  highlights: HighlightType[] | null | undefined;
  onHighlightButtonClick: (selection: Selection) => void;
  onMemoButtonClick: (selection: Selection) => void;
}

const ArticleContent = ({
  articleContent,
  highlights,
  onHighlightButtonClick,
  onMemoButtonClick,
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
        dangerouslySetInnerHTML={{
          __html: processContent(articleContent ?? ''),
        }}
      />
      <FloatingToolbar
        selectionTargetRef={contentRef}
        onHighlightButtonClick={onHighlightButtonClick}
        onMemoButtonClick={onMemoButtonClick}
      />
    </>
  );
};

export default ArticleContent;

const Container = styled.div`
  overflow: visible;

  display: flex;
  flex-direction: column;
  align-items: flex-start;

  white-space: normal;

  word-break: break-all;
  word-wrap: break-word;

  mark[data-highlight-id] {
    background-color: #ffeb3b;
    transition: box-shadow 0.2s ease-in-out;
  }

  mark[data-highlight-id].hovered-highlight {
    box-shadow: 0 0 6px rgb(0 0 0 / 30%);
    cursor: pointer;
  }
`;
