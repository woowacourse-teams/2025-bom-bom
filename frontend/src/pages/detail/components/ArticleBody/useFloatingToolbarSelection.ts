import { useCallback, useEffect, useRef } from 'react';
import { FloatingToolbarMode } from '../FloatingToolbar/FloatingToolbar.types';
import { useDevice } from '@/hooks/useDevice';
import { Position } from '@/types/position';

const TOOLBAR_HEIGHT = 40;

interface UseFloatingToolbarSelectionParams {
  isInSelectionTarget: (range: Range) => boolean;
  onShow: (params: { position: Position; mode: FloatingToolbarMode }) => void;
  onHide: () => void;
}

export const useFloatingToolbarSelection = ({
  isInSelectionTarget,
  onShow,
  onHide,
}: UseFloatingToolbarSelectionParams) => {
  const device = useDevice();
  const activeSelectionRangeRef = useRef<Range>(null);
  const activeHighlightIdRef = useRef<number>(null);

  const isPC = device === 'pc';

  const openToolbarFromSelection = useCallback(
    (selection: Selection) => {
      const range = selection.getRangeAt(0);
      if (!isInSelectionTarget(range)) return;

      const rect = range.getBoundingClientRect();

      onShow({
        position: {
          x: rect.left + rect.width / 2,
          y: isPC ? rect.top : rect.bottom + TOOLBAR_HEIGHT,
        },
        mode: 'new',
      });
      activeHighlightIdRef.current = null;
      activeSelectionRangeRef.current = range;
    },
    [isInSelectionTarget, isPC, onShow],
  );

  const openToolbarFromHighlight = useCallback(
    (target: HTMLElement) => {
      const id = target.dataset.highlightId;
      if (!id) return;

      const rect = target.getBoundingClientRect();
      onShow({
        position: {
          x: rect.left + rect.width / 2,
          y: isPC ? rect.top : rect.bottom + TOOLBAR_HEIGHT,
        },
        mode: 'existing',
      });
      activeHighlightIdRef.current = Number(id);
      activeSelectionRangeRef.current = null;
    },
    [isPC, onShow],
  );

  const handlePointerOrClick = useCallback(
    (e: PointerEvent | MouseEvent) => {
      const target = e.target as HTMLElement;

      // 하이라이트 클릭
      if (target.tagName === 'MARK') {
        openToolbarFromHighlight(target);
        return;
      }

      // 새 selection
      const selection = window.getSelection();
      if (selection && !selection.isCollapsed && selection.rangeCount > 0) {
        openToolbarFromSelection(selection);
        return;
      }
    },
    [openToolbarFromHighlight, openToolbarFromSelection],
  );

  const handleSelectionClear = useCallback(() => {
    const selection = window.getSelection();
    if (selection && selection.isCollapsed) {
      onHide();
    }
  }, [onHide]);

  useEffect(() => {
    document.addEventListener('mouseup', handlePointerOrClick);
    document.addEventListener('pointerup', handlePointerOrClick);
    document.addEventListener('selectionchange', handleSelectionClear);

    return () => {
      document.removeEventListener('mouseup', handlePointerOrClick);
      document.removeEventListener('pointerup', handlePointerOrClick);
      document.removeEventListener('selectionchange', handleSelectionClear);
    };
  }, [handlePointerOrClick, handleSelectionClear]);

  return {
    activeSelectionRange: activeSelectionRangeRef.current,
    activeHighlightId: activeHighlightIdRef.current,
  };
};
