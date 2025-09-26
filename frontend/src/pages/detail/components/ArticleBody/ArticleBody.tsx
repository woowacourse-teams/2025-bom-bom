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
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import type { GetArticleByIdResponse } from '@/apis/articles';

interface ArticleBodyProps {
  articleId: number;
  newsletterName: string;
  articleContent: GetArticleByIdResponse['contents'];
}

const ArticleBody = ({
  articleId,
  newsletterName,
  articleContent,
}: ArticleBodyProps) => {
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

  const addNewHighlight = (range: Range | null) => {
    if (!range) return;

    const highlightData = saveSelection(range, articleId);
    addHighlight(highlightData);
    window.getSelection()?.removeAllRanges();
  };

  const handleHighlightClick = () => {
    const isNewMode = mode === 'new';

    if (isNewMode) {
      addNewHighlight(activeSelectionRange);

      trackEvent({
        category: 'Memo',
        action: 'FloatingToolbar - 하이라이트 추가',
        label: '아티클 본문',
      });
    }
    if (!isNewMode && activeHighlightId) {
      removeHighlight({ id: activeHighlightId });

      trackEvent({
        category: 'Memo',
        action: 'FloatingToolbar - 하이라이트 삭제',
        label: '아티클 본문',
      });
    }

    hideToolbar();
  };

  const handleMemoClick = () => {
    const isNewMode = mode === 'new';

    if (isNewMode) {
      addNewHighlight(activeSelectionRange);

      trackEvent({
        category: 'Memo',
        action: 'FloatingToolbar - 메모 추가',
        label: '아티클 본문',
      });
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
      <ArticleContent
        ref={contentRef}
        newsletterName={newsletterName}
        content={articleContent}
      />
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
