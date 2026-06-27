import styled from '@emotion/styled';
import { ReactNode, useState } from 'react';
import { useClickOutsideRef } from '@/hooks/useClickOutsideRef';

interface FloatingActionButtonProps {
  icon: ReactNode;
  children: ReactNode;
}

const FloatingActionButton = ({
  icon,
  children,
}: FloatingActionButtonProps) => {
  const [isOpen, setIsOpen] = useState(false);

  const toggleMenu = () => {
    setIsOpen((prev) => !prev);
  };

  const floatingRef = useClickOutsideRef<HTMLDivElement>(() => {
    setIsOpen(false);
  });

  return (
    <div ref={floatingRef}>
      <FloatingButton onClick={toggleMenu}>{icon}</FloatingButton>
      {isOpen && <FloatingMenu>{children}</FloatingMenu>}
    </div>
  );
};

export default FloatingActionButton;

const FloatingButton = styled.button`
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 1000;
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

const FloatingMenu = styled.div`
  position: fixed;
  right: 20px;
  bottom: 88px;
  z-index: 999;
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
