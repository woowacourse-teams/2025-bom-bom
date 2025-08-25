import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';
import { PointerEvent, RefObject, useEffect, useRef, useState } from 'react';
import { FloatingToolbarMode } from './FloatingToolbar.types';
import { useDeviceType } from '@/hooks/useDeviceType';
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
  const selectionRef = useRef<Selection>(null);
  const [selectedHighlightId, setSelectedHighlightId] = useState<number | null>(
    null,
  );
  const [isVisible, setIsVisible] = useState(false);
  const [position, setPosition] = useState<ToolbarPosition>({ x: 0, y: 0 });
  const deviceType = useDeviceType();

  const hideToolbar = () => setIsVisible(false);
  const currentMode: FloatingToolbarMode = selectedHighlightId
    ? 'existing'
    : 'new';

  const handlePointerDownOnToolbar = (e: PointerEvent) => {
    e.preventDefault();
  };

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
      setPosition({
        x: rect.left + rect.width / 2,
        y: deviceType === 'mobile' ? rect.bottom + 40 : rect.top,
      });
      setIsVisible(true);
    };

    const handleTextSelection = (selection: Selection) => {
      selectionRef.current = selection;
      const range = selection.getRangeAt(0);
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

    const handleClickUp = (e: PointerEvent | MouseEvent) => {
      const target = e.target as HTMLElement;
      if (target.tagName === 'MARK') {
        handleHighlightClick(target);
        return;
      }

      const selection = window.getSelection();
      if (selection && !selection.isCollapsed && selection.rangeCount > 0) {
        handleTextSelection(selection);
        return;
      }

      hideToolbar();
    };

    document.addEventListener('mouseup', handleClickUp);
    document.addEventListener('pointerup', handleClickUp);
    return () => {
      document.removeEventListener('mouseup', handleClickUp);
      document.removeEventListener('pointerup', handleClickUp);
    };
  }, [deviceType, selectionTargetRef]);

  return (
    <Container
      position={position}
      visible={isVisible}
      onPointerDown={handlePointerDownOnToolbar}
    >
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
  z-index: ${({ theme }) => theme.zIndex.overlay};
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
