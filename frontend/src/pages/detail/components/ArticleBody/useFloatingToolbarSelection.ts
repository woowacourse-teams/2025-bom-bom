import { useCallback, useEffect, useRef } from 'react';
import { useDevice } from '@/hooks/useDevice';
import { isAndroid, isIOS, isWeb } from '@/libs/webview/webview.utils';
import type { FloatingToolbarMode } from '../FloatingToolbar/FloatingToolbar.types';
import type { Position } from '@/types/position';

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

  const handleHighlightClick = useCallback(
    (e: PointerEvent | MouseEvent) => {
      const target = e.target as HTMLElement;

      if (target.tagName === 'MARK') {
        openToolbarFromHighlight(target);
        return;
      }
    },
    [openToolbarFromHighlight],
  );

  const handleSelectionComplete = useCallback(() => {
    const selection = window.getSelection();
    if (selection && !selection.isCollapsed && selection.rangeCount > 0) {
      openToolbarFromSelection(selection);
      return;
    }
  }, [openToolbarFromSelection]);

  const handleSelectionClear = useCallback(() => {
    const selection = window.getSelection();
    if (selection && selection.isCollapsed) {
      onHide();
    }
  }, [onHide]);

  useEffect(() => {
    if (isAndroid()) {
      document.addEventListener('contextmenu', handleSelectionComplete);
      document.addEventListener('click', handleSelectionClear);
    } else {
      if (isWeb()) {
        document.addEventListener('mouseup', handleSelectionComplete);
      } else if (isIOS()) {
        document.addEventListener('pointerup', handleSelectionComplete);
      }
      document.addEventListener('selectionchange', handleSelectionClear);
    }
    document.addEventListener('click', handleHighlightClick);

    // logAllEvents();

    return () => {
      if (isAndroid()) {
        document.removeEventListener('contextmenu', handleSelectionComplete);
        document.removeEventListener('click', handleSelectionClear);
      } else {
        if (isWeb()) {
          document.removeEventListener('mouseup', handleSelectionComplete);
        } else if (isIOS()) {
          document.removeEventListener('pointerup', handleSelectionComplete);
        }
        document.removeEventListener('selectionchange', handleSelectionClear);
      }
      document.removeEventListener('click', handleHighlightClick);
    };
  }, [handleHighlightClick, handleSelectionClear, handleSelectionComplete]);

  return {
    activeSelectionRange: activeSelectionRangeRef.current,
    activeHighlightId: activeHighlightIdRef.current,
  };
};
