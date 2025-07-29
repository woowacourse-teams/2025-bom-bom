import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';
import { useEffect, useRef, useState } from 'react';

interface ToolbarPosition {
  x: number;
  y: number;
}

interface FloatingToolBarProps {
  onSave: (selection: Selection) => void;
}

export default function FloatingToolbar({ onSave }: FloatingToolBarProps) {
  const selectionRef = useRef<Selection>(null);
  const [isVisible, setIsVisible] = useState(false);
  const [position, setPosition] = useState<ToolbarPosition>({ x: 0, y: 0 });

  useEffect(() => {
    const handleMouseUp = () => {
      const selection = window.getSelection();
      if (selection && !selection.isCollapsed) {
        const range = selection.getRangeAt(0);
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

    document.addEventListener('mouseup', handleMouseUp);
    return () => document.removeEventListener('mouseup', handleMouseUp);
  }, []);

  return (
    <Container position={position} visible={isVisible}>
      <ToolbarButton
        onClick={() => {
          if (selectionRef.current === null) return;

          setIsVisible(false);
          onSave(selectionRef.current);
        }}
      >
        저장
      </ToolbarButton>
      <ToolbarButton onClick={() => alert('Action 2')}>🌐</ToolbarButton>
    </Container>
  );
}

// fade-in / fade-out keyframes
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

  display: flex;
  gap: 8px;

  padding: 8px 12px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgb(0 0 0 / 20%);

  background: #333;

  color: #fff;

  animation: ${({ visible }) => (visible ? fadeIn : fadeOut)} 0.2s ease-in-out
    forwards;

  opacity: ${({ visible }) => (visible ? 1 : 0)};
`;

const ToolbarButton = styled.button`
  border: none;

  background: transparent;

  color: #fff;
  font-size: 18px;

  cursor: pointer;
`;
