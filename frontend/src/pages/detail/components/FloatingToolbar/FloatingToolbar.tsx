import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';
import { useEffect, useRef, useState } from 'react';
import Comment from '#/assets/comment.svg';
import Pen from '#/assets/pen.svg';

interface ToolbarPosition {
  x: number;
  y: number;
}

interface FloatingToolBarProps {
  selectionTargetRef: React.RefObject<HTMLDivElement | null>;
  onHighlightButtonClick: (selection: Selection) => void;
  onMemoButtonClick: (selection: Selection) => void;
}

export default function FloatingToolbar({
  selectionTargetRef,
  onHighlightButtonClick,
  onMemoButtonClick,
}: FloatingToolBarProps) {
  const selectionRef = useRef<Selection>(null);
  const [isVisible, setIsVisible] = useState(false);
  const [position, setPosition] = useState<ToolbarPosition>({ x: 0, y: 0 });

  const handleHighlightButtonClick = () => {
    if (selectionRef.current === null) return;
    setIsVisible(false);
    onHighlightButtonClick(selectionRef.current);
  };

  const handleMemoButtonClick = () => {
    if (selectionRef.current === null) return;
    setIsVisible(false);
    onMemoButtonClick(selectionRef.current);
  };

  useEffect(() => {
    const handleClick = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (target.tagName === 'MARK' && target.dataset.highlightId) {
        const highlightId = target.dataset.highlightId;
        console.log('하이라이트 클릭됨', highlightId);
        setIsVisible(true);
        // TODO: 하이라이트 클릭 시 toolbar 열기 or 편집 모드
      }
    };

    const handleMouseUp = () => {
      const selection = window.getSelection();
      if (selection && !selection.isCollapsed) {
        const range = selection.getRangeAt(0);
        if (
          !selectionTargetRef.current?.contains(range.commonAncestorContainer)
        )
          return;

        const rect = range.getBoundingClientRect();

        setPosition({
          x: rect.left + rect.width / 2,
          y: rect.top,
        });
        setIsVisible(true);
        selectionRef.current = selection;
      } else {
        setIsVisible(false);
      }
    };

    document.addEventListener('click', handleClick);
    document.addEventListener('mouseup', handleMouseUp);
    return () => {
      document.removeEventListener('click', handleClick);
      document.removeEventListener('mouseup', handleMouseUp);
    };
  }, [selectionTargetRef]);

  return (
    <Container position={position} visible={isVisible}>
      <ToolbarButton onClick={handleHighlightButtonClick}>
        <Pen />
      </ToolbarButton>
      <ToolbarButton onClick={handleMemoButtonClick}>
        <Comment />
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
