import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';
import { PointerEvent, RefObject } from 'react';
import { FloatingToolbarMode } from './FloatingToolbar.types';
import {
  ToolbarPosition,
  useFloatingToolbarSelection,
} from './useFloatingToolbarSelection';
import MemoIcon from '#/assets/comment.svg';
import HighlightOffIcon from '#/assets/edit-off.svg';
import HighlightIcon from '#/assets/edit.svg';

interface FloatingToolBarProps {
  selectionTargetRef: RefObject<HTMLDivElement | null>;
  onHighlightButtonClick: ({
    mode,
    selectionRange,
    highlightId,
  }: {
    mode: FloatingToolbarMode;
    selectionRange: Range | null;
    highlightId: number | null;
  }) => void;
  onMemoButtonClick: ({
    mode,
    selectionRange,
  }: {
    mode: FloatingToolbarMode;
    selectionRange: Range | null;
  }) => void;
}

export default function FloatingToolbar({
  selectionTargetRef,
  onHighlightButtonClick,
  onMemoButtonClick,
}: FloatingToolBarProps) {
  const isInSelectionTarget = (range: Range) =>
    selectionTargetRef.current?.contains(range.commonAncestorContainer) ??
    false;

  const {
    isVisible,
    position,
    currentMode,
    handleHighlightButtonClick,
    handleMemoButtonClick,
  } = useFloatingToolbarSelection({
    isInSelectionTarget,
    onHighlightButtonClick,
    onMemoButtonClick,
  });

  const handlePointerDownOnToolbar = (e: PointerEvent) => {
    e.preventDefault();
  };

  return (
    <Container
      position={position}
      visible={isVisible}
      onPointerDown={handlePointerDownOnToolbar}
    >
      <ToolbarButton onClick={handleHighlightButtonClick}>
        {currentMode === 'new' ? <HighlightIcon /> : <HighlightOffIcon />}
      </ToolbarButton>
      <ToolbarButton onClick={handleMemoButtonClick}>
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
