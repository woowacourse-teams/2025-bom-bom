import { useCallback, useEffect } from 'react';
import { FloatingToolbarMode, ToolbarPosition } from './FloatingToolbar.types';
import { useDeviceType } from '@/hooks/useDeviceType';

interface UseFloatingToolbarSelectionParams {
  isInSelectionTarget: (range: Range) => boolean;
  onShow: (params: {
    position: ToolbarPosition;
    mode: FloatingToolbarMode;
    highlightId: number | null;
    range: Range | null;
  }) => void;
  onHide: () => void;
}

export const useFloatingToolbarSelection = ({
  isInSelectionTarget,
  onShow,
  onHide,
}: UseFloatingToolbarSelectionParams) => {
  const deviceType = useDeviceType();

  const openToolbarFromSelection = useCallback(
    (selection: Selection) => {
      const range = selection.getRangeAt(0);
      if (!isInSelectionTarget(range)) return;

      const rect = range.getBoundingClientRect();

      onShow({
        position: {
          x: rect.left + rect.width / 2,
          y: deviceType !== 'pc' ? rect.bottom + 40 : rect.top,
        },
        mode: 'new',
        highlightId: null,
        range,
      });
    },
    [deviceType, isInSelectionTarget, onShow],
  );

  const openToolbarFromHighlight = useCallback(
    (target: HTMLElement) => {
      const id = target.dataset.highlightId;
      if (!id) return;

      const rect = target.getBoundingClientRect();
      onShow({
        position: {
          x: rect.left + rect.width / 2,
          y: deviceType === 'mobile' ? rect.bottom + 40 : rect.top,
        },
        mode: 'existing',
        highlightId: Number(id),
        range: null,
      });
    },
    [deviceType, onShow],
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

      // 아무것도 없으면 툴바 닫기
      onHide();
    },
    [openToolbarFromHighlight, openToolbarFromSelection, onHide],
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
};
