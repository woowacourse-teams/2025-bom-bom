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

  const showToolbar = useCallback(
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

  const handleHighlightClick = useCallback(
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

  const handleSelectionEnd = useCallback(
    (e: PointerEvent | MouseEvent) => {
      const target = e.target as HTMLElement;
      if (target.tagName === 'MARK') {
        handleHighlightClick(target);
        return;
      }

      const selection = window.getSelection();
      if (selection && !selection.isCollapsed && selection.rangeCount > 0) {
        showToolbar(selection);
        return;
      }

      onHide();
    },
    [handleHighlightClick, onHide, showToolbar],
  );

  useEffect(() => {
    document.addEventListener('mouseup', handleSelectionEnd);
    document.addEventListener('pointerup', handleSelectionEnd);
    return () => {
      document.removeEventListener('mouseup', handleSelectionEnd);
      document.removeEventListener('pointerup', handleSelectionEnd);
    };
  }, [handleSelectionEnd]);
};
