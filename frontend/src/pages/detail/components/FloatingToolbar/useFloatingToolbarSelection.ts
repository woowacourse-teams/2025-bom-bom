import { useCallback, useEffect, useRef, useState } from 'react';
import { useDeviceType } from '@/hooks/useDeviceType';

export interface ToolbarPosition {
  x: number;
  y: number;
}

interface UseFloatingToolbarSelectionParams {
  isInSelectionTarget: (range: Range) => boolean;
}

export const useFloatingToolbarSelection = ({
  isInSelectionTarget,
}: UseFloatingToolbarSelectionParams) => {
  const rangeRef = useRef<Range>(null);
  const selectedHighlightIdRef = useRef<number>(null);
  const [isVisible, setIsVisible] = useState(false);
  const [position, setPosition] = useState<ToolbarPosition>({ x: 0, y: 0 });
  const deviceType = useDeviceType();

  const hideToolbar = useCallback(() => setIsVisible(false), []);

  const showToolbar = useCallback(
    (selection: Selection) => {
      if (selection.rangeCount === 0) return;

      const range = selection.getRangeAt(0);
      if (!isInSelectionTarget(range)) return;

      rangeRef.current = range;

      const rect = range.getBoundingClientRect();
      selectedHighlightIdRef.current = null;
      setPosition({
        x: rect.left + rect.width / 2,
        y: deviceType === 'mobile' ? rect.bottom + 40 : rect.top,
      });
      setIsVisible(true);
    },
    [deviceType, isInSelectionTarget],
  );

  const handleHighlightClick = useCallback(
    (target: HTMLElement) => {
      const id = target.dataset.highlightId;
      if (!id) return;

      selectedHighlightIdRef.current = Number(id);
      const rect = target.getBoundingClientRect();
      setPosition({
        x: rect.left + rect.width / 2,
        y: deviceType === 'mobile' ? rect.bottom + 40 : rect.top,
      });
      setIsVisible(true);
    },
    [deviceType],
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

  return {
    isVisible,
    position,
    selectionRange: rangeRef.current,
    selectedHighlightId: selectedHighlightIdRef.current,
    hideToolbar,
  };
};
