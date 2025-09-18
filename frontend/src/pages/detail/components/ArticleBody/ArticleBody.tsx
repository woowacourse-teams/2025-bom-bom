import { useEffect, useRef, useState } from 'react';
import { useFloatingToolbarSelection } from './useFloatingToolbarSelection';
import { useFloatingToolbarState } from '../../hooks/useFloatingToolbarState';
import { useHighlightData } from '../../hooks/useHighlightData';
import { restoreHighlightAll, saveSelection } from '../../utils/highlight';
import ArticleContent from '../ArticleContent/ArticleContent';
import FloatingToolbar from '../FloatingToolbar/FloatingToolbar';
import MemoPanel from '../MemoPanel/MemoPanel';
import { GetArticleByIdResponse } from '@/apis/articles';

interface ArticleBodyProps {
  articleId: number;
  articleContent: GetArticleByIdResponse['contents'];
}

const ArticleBody = ({ articleId, articleContent }: ArticleBodyProps) => {
  const contentRef = useRef<HTMLDivElement>(null);
  const { open, position, mode, showToolbar, hideToolbar } =
    useFloatingToolbarState();
  const [panelOpen, setPanelOpen] = useState(false);
  const { highlights, addHighlight, updateMemo, removeHighlight } =
    useHighlightData({ articleId });
  const { activeSelectionRange, activeHighlightId } =
    useFloatingToolbarSelection({
      isInSelectionTarget: (range) =>
        contentRef.current?.contains(range.commonAncestorContainer) ?? false,
      onShow: showToolbar,
      onHide: hideToolbar,
    });

  const handleHighlightClick = () => {
    if (mode === 'new' && activeSelectionRange) {
      const highlightData = saveSelection(activeSelectionRange, articleId);
      addHighlight(highlightData);
      window.getSelection()?.removeAllRanges();
    }
    if (mode === 'existing' && activeHighlightId) {
      removeHighlight(activeHighlightId);
    }

    hideToolbar();
  };

  const handleMemoClick = () => {
    if (mode === 'new' && activeSelectionRange) {
      const highlightData = saveSelection(activeSelectionRange, articleId);
      addHighlight(highlightData);
      window.getSelection()?.removeAllRanges();
    }

    setPanelOpen(true);
    hideToolbar();
  };

  useEffect(() => {
    if (!highlights || !articleContent) return;

    restoreHighlightAll(highlights);
  }, [articleContent, highlights]);

  return (
    <>
      <ArticleContent ref={contentRef} content={articleContent} />
      <FloatingToolbar
        open={open}
        position={position}
        mode={mode}
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
