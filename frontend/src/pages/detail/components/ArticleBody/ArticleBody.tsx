import { useEffect, useRef, useState } from 'react';
import { useHighlightData } from '../../hooks/useHighlightData';
import { restoreHighlightAll, saveSelection } from '../../utils/highlight';
import ArticleContent from '../ArticleContent/ArticleContent';
import FloatingToolbar from '../FloatingToolbar/FloatingToolbar';
import {
  FloatingToolbarMode,
  ToolbarPosition,
} from '../FloatingToolbar/FloatingToolbar.types';
import { useFloatingToolbarSelection } from '../FloatingToolbar/useFloatingToolbarSelection';
import MemoPanel from '../MemoPanel/MemoPanel';
import { GetArticleByIdResponse } from '@/apis/articles';

interface ArticleBodyProps {
  articleId: number;
  articleContent: GetArticleByIdResponse['contents'];
}

const ArticleBody = ({ articleId, articleContent }: ArticleBodyProps) => {
  const [isVisible, setIsVisible] = useState(false);
  const [position, setPosition] = useState<ToolbarPosition>({ x: 0, y: 0 });
  const [mode, setMode] = useState<FloatingToolbarMode>('new');
  const rangeRef = useRef<Range>(null);
  const selectedHighlightIdRef = useRef<number>(null);

  const [panelOpen, setPanelOpen] = useState(false);
  const contentRef = useRef<HTMLDivElement>(null);
  const { highlights, addHighlight, updateMemo, removeHighlight } =
    useHighlightData({ articleId });

  const hideToolbar = () => setIsVisible(false);

  const handleHighlightClick = () => {
    const isNewHighlight = mode === 'new';
    hideToolbar();

    if (isNewHighlight && rangeRef.current) {
      const highlightData = saveSelection(rangeRef.current, articleId);
      addHighlight(highlightData);
      window.getSelection()?.removeAllRanges();
    }
    if (!isNewHighlight && selectedHighlightIdRef.current) {
      removeHighlight(selectedHighlightIdRef.current);
    }
  };

  const handleMemoClick = () => {
    const isNewHighlight = mode === 'new';
    hideToolbar();

    if (isNewHighlight && rangeRef.current) {
      const highlightData = saveSelection(rangeRef.current, articleId);
      addHighlight(highlightData);
      window.getSelection()?.removeAllRanges();
    }
    setPanelOpen(true);
  };

  useFloatingToolbarSelection({
    isInSelectionTarget: (range) =>
      contentRef.current?.contains(range.commonAncestorContainer) ?? false,
    onShow: ({ position, mode, highlightId, range }) => {
      setPosition(position);
      setMode(mode);
      selectedHighlightIdRef.current = highlightId;
      rangeRef.current = range;
      setIsVisible(true);
    },
    onHide: () => {
      setIsVisible(false);
    },
  });

  useEffect(() => {
    if (!highlights || !articleContent) return;

    restoreHighlightAll(highlights);
  }, [articleContent, highlights]);

  return (
    <>
      <ArticleContent ref={contentRef} content={articleContent} />
      <FloatingToolbar
        visible={isVisible}
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
