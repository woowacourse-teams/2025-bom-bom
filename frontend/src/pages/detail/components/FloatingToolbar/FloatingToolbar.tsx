import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';
import { PointerEvent } from 'react';
import { FloatingToolbarMode } from './FloatingToolbar.types';
import { Position } from '@/types/position';
import MemoIcon from '#/assets/comment.svg';
import HighlightOffIcon from '#/assets/edit-off.svg';
import HighlightIcon from '#/assets/edit.svg';

interface FloatingToolBarProps {
  open: boolean;
  position: Position;
  mode: FloatingToolbarMode;
  onHighlightButtonClick: () => void;
  onMemoButtonClick: () => void;
}

const FloatingToolbar = ({
  open,
  position,
  mode,
  onHighlightButtonClick,
  onMemoButtonClick,
}: FloatingToolBarProps) => {
  const isNewMode = mode === 'new';

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
        {isNewMode ? <HighlightIcon /> : <HighlightOffIcon />}
      </ToolbarButton>
      <ToolbarButton onClick={onMemoButtonClick}>
        <MemoIcon />
      </ToolbarButton>
    </Container>
  );
};

export default FloatingToolbar;

const fadeIn = keyframes`
    from { opacity: 0; transform: translate(-50%, -90%); }
    to { opacity: 1; transform: translate(-50%, -100%); }
  `;

const fadeOut = keyframes`
    from { opacity: 1; transform: translate(-50%, -100%); }
    to { opacity: 0; transform: translate(-50%, -90%); }
  `;

const Container = styled.div<{ position: Position; open: boolean }>`
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
