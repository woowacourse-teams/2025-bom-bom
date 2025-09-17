import { useEffect, useRef, useState } from 'react';
import { useHighlightData } from '../../hooks/useHighlightData';
import { restoreHighlightAll, saveSelection } from '../../utils/highlight';
import ArticleContent from '../ArticleContent/ArticleContent';
import FloatingToolbar from '../FloatingToolbar/FloatingToolbar';
import { FloatingToolbarMode } from '../FloatingToolbar/FloatingToolbar.types';
import MemoPanel from '../MemoPanel/MemoPanel';
import { GetArticleByIdResponse } from '@/apis/articles';

interface ArticleBodyProps {
  articleId: number;
  articleContent: GetArticleByIdResponse['contents'];
}

const ArticleBody = ({ articleId, articleContent }: ArticleBodyProps) => {
  const [panelOpen, setPanelOpen] = useState(false);
  const contentRef = useRef<HTMLDivElement>(null);
  const { highlights, addHighlight, updateMemo, removeHighlight } =
    useHighlightData({ articleId });
  const handleHighlightClick = ({
    mode,
    selectionRange,
    highlightId,
  }: {
    mode: FloatingToolbarMode;
    selectionRange: Range | null;
    highlightId: number | null;
  }) => {
    const isNewHighlight = mode === 'new';

    if (isNewHighlight && selectionRange) {
      const highlightData = saveSelection(selectionRange, articleId);
      addHighlight(highlightData);
    }
    if (!isNewHighlight && highlightId) {
      removeHighlight(highlightId);
    }
  };

  const handleMemoClick = ({
    mode,
    selectionRange,
  }: {
    mode: FloatingToolbarMode;
    selectionRange: Range | null;
  }) => {
    const isNewHighlight = mode === 'new';

    if (isNewHighlight && selectionRange) {
      const highlightData = saveSelection(selectionRange, articleId);
      addHighlight(highlightData);
    }
    setPanelOpen(true);
  };

  useEffect(() => {
    if (!highlights || !articleContent) return;

    restoreHighlightAll(highlights);
  }, [articleContent, highlights]);

  return (
    <>
      <ArticleContent ref={contentRef} content={articleContent} />
      <FloatingToolbar
        selectionTargetRef={contentRef}
        onHighlightButtonClick={handleHighlightClick}
        onMemoButtonClick={handleMemoClick}
      />
      <MemoPanel
        open={panelOpen}
        memos={highlights ?? []}
        removeHighlight={removeHighlight}
        updateMemo={updateMemo}
        handleClose={() => setPanelOpen(false)}
        handleToggle={() => setPanelOpen((prev) => !prev)}
      />
    </>
  );
};

export default ArticleBody;
