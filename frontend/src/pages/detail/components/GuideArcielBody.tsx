import { useEffect, useRef, useState } from 'react';
import { useHighlightHoverEffect } from '../hooks/useHighlightHoverEffect';
import { ArticleDetail } from '../types/articleDetail';
import { FloatingToolbarMode } from './FloatingToolbar/FloatingToolbar.types';
import { useLocalHighlighData } from '../hooks/useLocalHighlightData';
import { restoreHighlightAll, saveSelection } from '../utils/highlight';
import ArticleContent from './ArticleContent/ArticleContent';
import FloatingToolbar from './FloatingToolbar/FloatingToolbar';
import MemoPanel from './MemoPanel/MemoPanel';

interface ArticleBodyProps {
  articleId: number;
  articleContent: ArticleDetail['contents'];
}

const GuideArticleBody = ({ articleId, articleContent }: ArticleBodyProps) => {
  const [panelOpen, setPanelOpen] = useState(false);
  const contentRef = useRef<HTMLDivElement>(null);
  const { highlights, addHighlight, updateMemo, removeHighlight } =
    useLocalHighlighData({ articleId });

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
    setPanelOpen(true);
  };

  useHighlightHoverEffect();

  useEffect(() => {
    if (!highlights || !articleContent) return;

    restoreHighlightAll(highlights);
  }, [articleContent, highlights]);

  return (
    <>
      <ArticleContent ref={contentRef} content={articleContent} />
      <FloatingToolbar
        selectionTargetRef={contentRef}
        onHighlightClick={handleHighlightClick}
        onMemoClick={handleMemoClick}
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

export default GuideArticleBody;
