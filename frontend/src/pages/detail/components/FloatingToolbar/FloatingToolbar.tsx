import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';
import { PointerEvent } from 'react';
import { FloatingToolbarMode, ToolbarPosition } from './FloatingToolbar.types';
import MemoIcon from '#/assets/comment.svg';
import HighlightOffIcon from '#/assets/edit-off.svg';
import HighlightIcon from '#/assets/edit.svg';

interface FloatingToolBarProps {
  open: boolean;
  position: ToolbarPosition;
  mode: FloatingToolbarMode;
  onHighlightButtonClick: () => void;
  onMemoButtonClick: () => void;
}

export default function FloatingToolbar({
  open,
  position,
  mode,
  onHighlightButtonClick,
  onMemoButtonClick,
}: FloatingToolBarProps) {
  const handlePointerDownOnToolbar = (e: PointerEvent) => {
    e.preventDefault();
  };

  return (
    <Container
      position={position}
      open={open}
      onPointerDown={handlePointerDownOnToolbar}
    >
      <ToolbarButton onClick={onHighlightButtonClick}>
        {mode === 'new' ? <HighlightIcon /> : <HighlightOffIcon />}
      </ToolbarButton>
      <ToolbarButton onClick={onMemoButtonClick}>
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

const Container = styled.div<{ position: ToolbarPosition; open: boolean }>`
  position: fixed;
  top: ${({ position }) => position.y}px;
  left: ${({ position }) => position.x}px;
  z-index: ${({ theme }) => theme.zIndex.overlay};
  padding: 6px 10px 4px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgb(0 0 0 / 20%);

  display: ${({ open }) => (open ? 'flex' : 'none')};
  gap: 8px;

  background: ${({ theme }) => theme.colors.primary};

  animation: ${({ open }) => (open ? fadeIn : fadeOut)} 0.2s ease-in-out
    forwards;
`;

const ToolbarButton = styled.button``;
