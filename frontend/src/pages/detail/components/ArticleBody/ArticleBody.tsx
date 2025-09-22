import { useEffect, useRef, useState } from 'react';
import { useFloatingToolbarSelection } from './useFloatingToolbarSelection';
import { useAddHighlightMutation } from '../../hooks/useAddHighlightMutation';
import { useFloatingToolbarState } from '../../hooks/useFloatingToolbarState';
import { useHighlights } from '../../hooks/useHighlights';
import { useRemoveHighlightMutation } from '../../hooks/useRemoveHighlighMutation';
import { useUpdateHighlightMutation } from '../../hooks/useUpdateHighlightMutation';
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
  const {
    opened: toolbarOpened,
    position,
    mode,
    showToolbar,
    hideToolbar,
  } = useFloatingToolbarState();
  const [panelOpen, setPanelOpen] = useState(false);
  const { highlights, isHighlightLoaded } = useHighlights({ articleId });
  const { mutate: addHighlight } = useAddHighlightMutation();
  const { mutate: updateHighlight } = useUpdateHighlightMutation();
  const { mutate: removeHighlight } = useRemoveHighlightMutation({ articleId });
  const { activeSelectionRange, activeHighlightId } =
    useFloatingToolbarSelection({
      isInSelectionTarget: (range) =>
        contentRef.current?.contains(range.commonAncestorContainer) ?? false,
      onShow: showToolbar,
      onHide: hideToolbar,
    });

  const updateMemo = (id: number, memo: string) => {
    updateHighlight({ id, memo });
  };

  const handleHighlightClick = () => {
    const isNewMode = mode === 'new';

    if (isNewMode && activeSelectionRange) {
      const highlightData = saveSelection(activeSelectionRange, articleId);
      addHighlight(highlightData);
      window.getSelection()?.removeAllRanges();
    }
    if (!isNewMode && activeHighlightId) {
      removeHighlight({ id: activeHighlightId });
    }

    hideToolbar();
  };

  const handleMemoClick = () => {
    const isNewMode = mode === 'new';

    if (isNewMode && activeSelectionRange) {
      const highlightData = saveSelection(activeSelectionRange, articleId);
      addHighlight(highlightData);
      window.getSelection()?.removeAllRanges();
    }

    setPanelOpen(true);
    hideToolbar();
  };

  useEffect(() => {
    if (!articleContent) return;

    if (isHighlightLoaded) restoreHighlightAll(highlights);

    // 하이라이트가 처음 로드될 때만 restore 실행 (highlight 변경 시 재실행 방지)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [articleContent, isHighlightLoaded]);

  return (
    <>
      <ArticleContent ref={contentRef} content={articleContent} />
      <FloatingToolbar
        opened={toolbarOpened}
        position={position}
        mode={mode}
        onHighlightButtonClick={handleHighlightClick}
        onMemoButtonClick={handleMemoClick}
      />
      <MemoPanel
        opened={panelOpen}
        memos={highlights ?? []}
        removeHighlight={removeHighlight}
        updateMemo={updateMemo}
        onCloseButtonClick={() => setPanelOpen(false)}
        onToggleButtonClick={() => setPanelOpen((prev) => !prev)}
      />
    </>
  );
};

export default ArticleBody;
