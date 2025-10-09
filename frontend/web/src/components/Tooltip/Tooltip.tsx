import { css } from '@emotion/react';
import styled from '@emotion/styled';
import type { PropsWithChildren } from 'react';

interface TooltipProps {
  id?: string;
  opened: boolean;
  position?: 'top' | 'bottom' | 'left' | 'right';
}

const Tooltip = ({
  id,
  opened,
  position = 'top',
  children,
}: PropsWithChildren<TooltipProps>) => {
  return (
    <Container role="tooltip" id={id} opened={opened} position={position}>
      {children}
    </Container>
  );
};

export default Tooltip;

const Container = styled.div<{ opened: boolean; position: string }>`
  visibility: hidden;
  position: absolute;
  z-index: ${({ theme }) => theme.zIndex.elevated};
  width: max-content;
  max-width: 280px;
  padding: 10px 12px;
  border-radius: 10px;
  box-shadow: 0 10px 20px -12px rgb(0 0 0 / 35%);

  background: ${({ theme }) => theme.colors.black};
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.caption};

  opacity: 0;
  transform: translateY(4px);
  transition:
    opacity 0.15s ease,
    transform 0.15s ease,
    visibility 0.15s;

  ${({ opened }) =>
    opened &&
    css`
      visibility: visible;

      opacity: 1;
      transform: translateY(0);
    `}

  ${({ position }) =>
    position === 'top' &&
    css`
      bottom: 28px;
      left: 0;
    `}
    
  ${({ position }) =>
    position === 'bottom' &&
    css`
      top: 28px;
      left: 0;
    `}
    
  ${({ position }) =>
    position === 'left' &&
    css`
      top: 50%;
      right: 100%;

      transform: translate(-8px, -50%);
    `}
    
  ${({ position }) =>
    position === 'right' &&
    css`
      top: 50%;
      left: 100%;

      transform: translate(8px, -50%);
    `}
`;
