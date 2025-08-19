import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';
import { RefObject, useEffect, useRef, useState } from 'react';
import { FloatingToolbarMode } from './FloatingToolbar.types';
import MemoIcon from '#/assets/comment.svg';
import HighlightOffIcon from '#/assets/edit-off.svg';
import HighlightIcon from '#/assets/edit.svg';

interface ToolbarPosition {
  x: number;
  y: number;
}

interface FloatingToolBarProps {
  selectionTargetRef: RefObject<HTMLDivElement | null>;
  onHighlightClick: ({
    mode,
    selection,
    highlightId,
  }: {
    mode: FloatingToolbarMode;
    selection: Selection | null;
    highlightId: number | null;
  }) => void;
  onMemoClick: ({
    mode,
    selection,
  }: {
    mode: FloatingToolbarMode;
    selection: Selection | null;
  }) => void;
}

export default function FloatingToolbar({
  selectionTargetRef,
  onHighlightClick,
  onMemoClick,
}: FloatingToolBarProps) {
  const selectionRef = useRef<Selection | null>(null);
  const [selectedHighlightId, setSelectedHighlightId] = useState<number | null>(
    null,
  );
  const [isVisible, setIsVisible] = useState(false);
  const [position, setPosition] = useState<ToolbarPosition>({ x: 0, y: 0 });

  const hideToolbar = () => setIsVisible(false);
  const currentMode: FloatingToolbarMode = selectedHighlightId
    ? 'existing'
    : 'new';

  const handleHighlightClick = () => {
    hideToolbar();
    onHighlightClick({
      mode: currentMode,
      selection: selectionRef.current,
      highlightId: selectedHighlightId,
    });
    window.getSelection()?.removeAllRanges();
  };

  const handleMemoClick = () => {
    hideToolbar();
    onMemoClick({
      mode: currentMode,
      selection: selectionRef.current,
    });
    window.getSelection()?.removeAllRanges();
  };

  useEffect(() => {
    const showToolbarAt = (rect: DOMRect) => {
      setPosition({ x: rect.left + rect.width / 2, y: rect.top });
      setIsVisible(true);
    };

    const handleTextSelection = (selection: Selection) => {
      if (!selection || selection.isCollapsed || selection.rangeCount === 0) {
        return;
      }
      selectionRef.current = selection;
      let range: Range;
      try {
        range = selection.getRangeAt(0);
      } catch {
        return;
      }
      if (!selectionTargetRef.current?.contains(range.commonAncestorContainer))
        return;

      const rect = range.getBoundingClientRect();
      setSelectedHighlightId(null);
      showToolbarAt(rect);
    };

    const handleHighlightClick = (target: HTMLElement) => {
      const id = target.dataset.highlightId;
      if (!id) return;

      setSelectedHighlightId(Number(id));
      const rect = target.getBoundingClientRect();
      showToolbarAt(rect);
    };

    const handlePointerUp = (e: PointerEvent | MouseEvent | TouchEvent) => {
      // Increased timeout for mobile browsers to finalize selection
      const timeout = e.type === 'touchend' ? 100 : 0;
      setTimeout(() => {
        const selection = window.getSelection();
        if (selection && !selection.isCollapsed) {
          handleTextSelection(selection);
          return;
        }

        const target = e.target as HTMLElement;
        if (target && target.tagName === 'MARK') {
          handleHighlightClick(target);
          return;
        }

        hideToolbar();
      }, timeout);
    };

    const handleSelectionChange = () => {
      // 모바일에서 selectionchange 이벤트의 안정성을 위한 디바운싱
      setTimeout(() => {
        const selection = window.getSelection();
        if (!selection || selection.isCollapsed || selection.rangeCount === 0) {
          return;
        }
        handleTextSelection(selection);
      }, 50);
    };

    // 모바일 터치 이벤트 추가
    const handleTouchStart = () => {
      // 터치 시작 시 기존 툴바 숨기기
      hideToolbar();
    };

    document.addEventListener('selectionchange', handleSelectionChange);
    document.addEventListener('pointerup', handlePointerUp as EventListener);
    document.addEventListener('mouseup', handlePointerUp as EventListener);
    document.addEventListener('touchend', handlePointerUp as EventListener);
    document.addEventListener('touchstart', handleTouchStart as EventListener);
    return () => {
      document.removeEventListener('selectionchange', handleSelectionChange);
      document.removeEventListener(
        'pointerup',
        handlePointerUp as EventListener,
      );
      document.removeEventListener('mouseup', handlePointerUp as EventListener);
      document.removeEventListener(
        'touchend',
        handlePointerUp as EventListener,
      );
      document.removeEventListener(
        'touchstart',
        handleTouchStart as EventListener,
      );
    };
  }, [selectionTargetRef]);

  return (
    <Container position={position} visible={isVisible}>
      <ToolbarButton onClick={handleHighlightClick}>
        {currentMode === 'new' ? <HighlightIcon /> : <HighlightOffIcon />}
      </ToolbarButton>
      <ToolbarButton onClick={handleMemoClick}>
        <MemoIcon />
      </ToolbarButton>
    </Container>
  );
}

const fadeIn = keyframes`
    from { opacity: 0; transform: translate(-50%, -90%); }
    to { opacity: 1; transform: translate(-50%, -100%); }
  `;

const fadeOut = keyframes`
    from { opacity: 1; transform: translate(-50%, -100%); }
    to { opacity: 0; transform: translate(-50%, -90%); }
  `;

const Container = styled.div<{ position: ToolbarPosition; visible: boolean }>`
  position: fixed;
  top: ${({ position }) => position.y}px;
  left: ${({ position }) => position.x}px;
  z-index: 1000;
  padding: 6px 10px 4px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgb(0 0 0 / 20%);

  display: flex;
  gap: 8px;

  background: ${({ theme }) => theme.colors.primary};

  animation: ${({ visible }) => (visible ? fadeIn : fadeOut)} 0.2s ease-in-out
    forwards;
`;

const ToolbarButton = styled.button``;
