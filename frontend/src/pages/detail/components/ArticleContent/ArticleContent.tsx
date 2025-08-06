import styled from '@emotion/styled';
import { useEffect, useRef, useState } from 'react';
import { useHighlightData } from '../../hooks/useHighlightData';
import { processContent } from './ArticleContent.utils';
import { useHighlightHoverEffect } from '../../hooks/useHighlightHoverEffect';
import { restoreHighlight, saveSelection } from '../../utils/highlight';
import FloatingToolbar from '../FloatingToolbar/FloatingToolbar';
import { FloatingToolbarMode } from '../FloatingToolbar/FloatingToolbar.types';
import MemoPanel from '../MemoPanel/MemoPanel';
import { components } from '@/types/openapi';

interface ArticleContentProps {
  articleId: number;
  articleContent: components['schemas']['ArticleDetailResponse']['contents'];
}

const ArticleContent = ({ articleId, articleContent }: ArticleContentProps) => {
  const [open, setOpen] = useState(false);
  const contentRef = useRef<HTMLDivElement>(null);
  const { highlights, addHighlight, updateMemo, removeHighlight } =
    useHighlightData({ articleId });

  const handleHighlightClick = ({
    mode,
    selection,
    highlightId,
  }: {
    mode: FloatingToolbarMode;
    selection: Selection | null;
    highlightId: number | null;
  }) => {
    const isNewHighlight = mode === 'new';

    if (isNewHighlight && selection) {
      const highlightData = saveSelection(selection, articleId);
      addHighlight(highlightData);
    }
    if (!isNewHighlight && highlightId) {
      removeHighlight(highlightId);
    }
  };

  const handleMemoClick = ({
    mode,
    selection,
  }: {
    mode: FloatingToolbarMode;
    selection: Selection | null;
  }) => {
    const isNewHighlight = mode === 'new';

    if (isNewHighlight && selection) {
      const highlightData = saveSelection(selection, articleId);
      addHighlight(highlightData);
    }
    setOpen(true);
  };

  useHighlightHoverEffect();

  useEffect(() => {
    if (!highlights || highlights?.length === 0 || !articleContent) return;

    try {
      highlights.forEach((highlight) => restoreHighlight(highlight));
    } catch (error) {
    }
      console.error(error);
  }, [articleContent, highlights, open]);

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
        onHighlightClick={handleHighlightClick}
        onMemoClick={handleMemoClick}
      />
      <MemoPanel
        open={open}
        handleClose={() => setOpen(false)}
        memos={highlights ?? []}
        removeHighlight={removeHighlight}
        updateMemo={updateMemo}
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
