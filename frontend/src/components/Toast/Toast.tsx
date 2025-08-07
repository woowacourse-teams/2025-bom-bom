import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';

interface ToastProps {
  message: string;
  duration: number;
  isVisible: boolean;
}

const Toast = ({ message, duration, isVisible }: ToastProps) => {
  if (!isVisible) return;

  return <Container duration={duration}>{message}</Container>;
};

export default Toast;

const toastAnimation = keyframes`
  0% {
    opacity: 0;
    transform: translateY(-10px);
  }
  20% {
    opacity: 1;
    transform: translateY(0);
  }
  80% {
    opacity: 1;
    transform: translateY(0);
  }
  100% {
    opacity: 0;
    transform: translateY(-10px);
  }
`;

const Container = styled.div<{ duration: number }>`
  position: fixed;
  top: 100px;
  z-index: 1000;
  width: 250px;
  padding: 12px 0;

  background-color: ${({ theme }) => theme.colors.primaryLight};
  color: ${({ theme }) => theme.colors.textPrimary};
  text-align: center;

  animation: ${toastAnimation} ${({ duration }) => duration}ms ease-in-out
    forwards;

  box-sizing: border-box;
`;
