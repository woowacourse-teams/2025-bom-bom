import { useEffect, useRef, useState } from 'react';
import { useFloatingToolbarState } from '../../hooks/useFloatingToolbarState';
import { useHighlightData } from '../../hooks/useHighlightData';
import { restoreHighlightAll, saveSelection } from '../../utils/highlight';
import ArticleContent from '../ArticleContent/ArticleContent';
import FloatingToolbar from '../FloatingToolbar/FloatingToolbar';
import { useFloatingToolbarSelection } from '../FloatingToolbar/useFloatingToolbarSelection';
import MemoPanel from '../MemoPanel/MemoPanel';
import { GetArticleByIdResponse } from '@/apis/articles';

interface ArticleBodyProps {
  articleId: number;
  articleContent: GetArticleByIdResponse['contents'];
}

const ArticleBody = ({ articleId, articleContent }: ArticleBodyProps) => {
  const { open, position, mode, showToolbar, hideToolbar } =
    useFloatingToolbarState();
  const rangeRef = useRef<Range>(null);
  const selectedHighlightIdRef = useRef<number>(null);

  const [panelOpen, setPanelOpen] = useState(false);
  const contentRef = useRef<HTMLDivElement>(null);
  const { highlights, addHighlight, updateMemo, removeHighlight } =
    useHighlightData({ articleId });

  const handleHighlightClick = () => {
    if (mode === 'new' && rangeRef.current) {
      const highlightData = saveSelection(rangeRef.current, articleId);
      addHighlight(highlightData);
      window.getSelection()?.removeAllRanges();
    }
    if (mode === 'existing' && selectedHighlightIdRef.current) {
      removeHighlight(selectedHighlightIdRef.current);
    }

    hideToolbar();
  };

  const handleMemoClick = () => {
    if (mode === 'new' && rangeRef.current) {
      const highlightData = saveSelection(rangeRef.current, articleId);
      addHighlight(highlightData);
      window.getSelection()?.removeAllRanges();
    }

    setPanelOpen(true);
    hideToolbar();
  };

  useFloatingToolbarSelection({
    isInSelectionTarget: (range) =>
      contentRef.current?.contains(range.commonAncestorContainer) ?? false,
    onShow: ({ position, mode, highlightId, range }) => {
      showToolbar({ position, mode });
      selectedHighlightIdRef.current = highlightId;
      rangeRef.current = range;
    },
    onHide: hideToolbar,
  });

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
