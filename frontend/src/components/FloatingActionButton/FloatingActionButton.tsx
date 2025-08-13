import styled from '@emotion/styled';
import { useState, ReactNode } from 'react';

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
    setIsOpen(!isOpen);
  };

  return (
    <>
      <FloatingButton onClick={toggleMenu}>{icon}</FloatingButton>
      {isOpen && (
        <FloatingMenu onClick={() => setIsOpen(false)}>{children}</FloatingMenu>
      )}
    </>
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

  cursor: pointer;

  &:hover {
    box-shadow: 0 6px 16px rgb(0 0 0 / 20%);
  }

  svg {
    color: ${({ theme }) => theme.colors.white};
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
