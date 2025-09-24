import styled from '@emotion/styled';
import { useState } from 'react';
import { useClickOutsideRef } from '@/hooks/useClickOutsideRef';
import { useDevice } from '@/hooks/useDevice';
import type { Device } from '@/hooks/useDevice';
import type { ReactNode } from 'react';

interface FloatingActionButtonProps {
  icon: ReactNode;
  children: ReactNode;
}

const FloatingActionButton = ({
  icon,
  children,
}: FloatingActionButtonProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const device = useDevice();

  const toggleMenu = () => {
    setIsOpen((prev) => !prev);
  };

  const floatingRef = useClickOutsideRef<HTMLDivElement>(() => {
    setIsOpen(false);
  });

  return (
    <div ref={floatingRef}>
      <FloatingButton onClick={toggleMenu} device={device}>
        {icon}
      </FloatingButton>
      {isOpen && <FloatingMenu device={device}>{children}</FloatingMenu>}
    </div>
  );
};

export default FloatingActionButton;

const FloatingButton = styled.button<{ device: Device }>`
  position: fixed;
  right: 20px;
  bottom: calc(
    ${({ theme }) => theme.heights.bottomNav} + env(safe-area-inset-bottom) +
      24px
  );
  z-index: ${({ theme }) => theme.zIndex.overlay};
  width: 56px;
  height: 56px;
  border: none;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgb(0 0 0 / 15%);

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};

  &:hover {
    box-shadow: 0 6px 16px rgb(0 0 0 / 20%);
  }
`;

const FloatingMenu = styled.div<{ device: Device }>`
  position: fixed;
  right: 20px;
  bottom: calc(152px + env(safe-area-inset-bottom));
  bottom: calc(
    ${({ theme }) => theme.heights.bottomNav} + env(safe-area-inset-bottom) +
      92px
  );
  z-index: ${({ theme }) => theme.zIndex.floating};
  min-width: 120px;
  padding: 12px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 12px;
  box-shadow: 0 4px 12px rgb(0 0 0 / 15%);

  display: flex;
  gap: 8px;
  flex-direction: column;

  background-color: ${({ theme }) => theme.colors.white};
`;
