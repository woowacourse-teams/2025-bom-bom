import { useCallback, useEffect, useRef } from 'react';
import { useDevice } from '@/hooks/useDevice';
import { isAndroid, isIOS, isWeb } from '@/libs/webview/webview.utils';
import type { FloatingToolbarMode } from '../FloatingToolbar/FloatingToolbar.types';
import type { Position } from '@/types/position';
import type { RefObject } from 'react';

const TOOLBAR_HEIGHT = 40;

interface UseFloatingToolbarSelectionParams {
  contentRef: RefObject<HTMLDivElement | null>;
  onShow: (params: { position: Position; mode: FloatingToolbarMode }) => void;
  onHide: () => void;
}

export const useFloatingToolbarSelection = ({
  contentRef,
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
    [isPC, onShow],
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

      onHide();
    },
    [openToolbarFromHighlight, onHide],
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

  const handleHighlightClickOrSelection = useCallback(
    (e: PointerEvent | MouseEvent) => {
      const target = e.target as HTMLElement;

      if (target.tagName === 'MARK') {
        openToolbarFromHighlight(target);
        return;
      }

      const selection = window.getSelection();
      if (selection && !selection.isCollapsed && selection.rangeCount > 0) {
        openToolbarFromSelection(selection);
        return;
      }

      onHide();
    },
    [onHide, openToolbarFromHighlight, openToolbarFromSelection],
  );

  useEffect(() => {
    const contentEl = contentRef.current;
    if (!contentEl) return;

    if (isIOS()) {
      contentEl.addEventListener('pointerup', handleHighlightClickOrSelection);
    } else if (isAndroid()) {
      contentEl.addEventListener('contextmenu', handleSelectionComplete);
      contentEl.addEventListener('click', handleHighlightClick);
    } else if (isWeb()) {
      contentEl.addEventListener('mouseup', handleHighlightClickOrSelection);
      document.addEventListener('selectionchange', handleSelectionClear);
    }

    return () => {
      if (!contentEl) return;

      if (isIOS()) {
        contentEl.removeEventListener(
          'pointerup',
          handleHighlightClickOrSelection,
        );
      } else if (isAndroid()) {
        contentEl.removeEventListener('contextmenu', handleSelectionComplete);
        contentEl.removeEventListener('click', handleHighlightClick);
      } else if (isWeb()) {
        contentEl.removeEventListener(
          'mouseup',
          handleHighlightClickOrSelection,
        );
        document.removeEventListener('selectionchange', handleSelectionClear);
      }
    };
  }, [
    contentRef,
    handleHighlightClick,
    handleHighlightClickOrSelection,
    handleSelectionClear,
    handleSelectionComplete,
  ]);

  return {
    activeSelectionRange: activeSelectionRangeRef.current,
    activeHighlightId: activeHighlightIdRef.current,
  };
};
