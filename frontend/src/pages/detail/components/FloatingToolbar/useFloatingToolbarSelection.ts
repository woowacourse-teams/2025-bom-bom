import { useCallback, useEffect } from 'react';
import { DeviceType } from '@/hooks/useDeviceType';

interface UseFloatingToolbarSelectionParams {
  deviceType: DeviceType;
  showToolbarAt: (x: number, y: number) => void;
  hideToolbar: () => void;
  isInSelectionTarget: (range: Range) => boolean;
  setRange: (range: Range) => void;
  updateSelectedHighlighId: (id: number | null) => void;
}

export const useFloatingToolbarSelection = ({
  deviceType,
  showToolbarAt,
  hideToolbar,
  isInSelectionTarget,
  setRange,
  updateSelectedHighlighId,
}: UseFloatingToolbarSelectionParams) => {
  const showToolbar = useCallback(
    (selection: Selection) => {
      if (selection.rangeCount === 0) return;

      const range = selection.getRangeAt(0);
      if (!isInSelectionTarget(range)) return;

      setRange(range);

      const rect = range.getBoundingClientRect();
      updateSelectedHighlighId(null);
      showToolbarAt(
        rect.left + rect.width / 2,
        deviceType === 'mobile' ? rect.bottom + 40 : rect.top,
      );
    },
    [
      deviceType,
      isInSelectionTarget,
      setRange,
      showToolbarAt,
      updateSelectedHighlighId,
    ],
  );

  const handleHighlightClick = useCallback(
    (target: HTMLElement) => {
      const id = target.dataset.highlightId;
      if (!id) return;

      updateSelectedHighlighId(Number(id));
      const rect = target.getBoundingClientRect();
      showToolbarAt(
        rect.left + rect.width / 2,
        deviceType === 'mobile' ? rect.bottom + 40 : rect.top,
      );
    },
    [deviceType, showToolbarAt, updateSelectedHighlighId],
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

      hideToolbar();
    },
    [handleHighlightClick, hideToolbar, showToolbar],
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
