import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';
import { useEffect } from 'react';
import { ToastData, ToastType } from './Toast.types';
import { hideToast } from './utils/toastActions';
import { theme } from '@/styles/theme';

const iconMap: Record<ToastType, string> = {
  error: '/assets/cancel-circle.svg',
  info: '/assets/info-circle.svg',
  success: '/assets/check-circle.svg',
};

const ToastItem = ({
  toast,
  isTop,
  duration,
}: {
  toast: ToastData;
  isTop: boolean;
  duration: number;
}) => {
  useEffect(() => {
    const timerId = setTimeout(() => {
      if (toast.id) hideToast(toast.id);
    }, duration);
    return () => {
      clearTimeout(timerId);
    };
  }, [duration, toast.id]);

  return (
    <Container enterFromTop={isTop} type={toast.type}>
      <ToastIcon src={iconMap[toast.type]} />
      <Content>{toast.message}</Content>
      <ProgressBar type={toast.type} duration={duration / 1000} />
    </Container>
  );
};

export default ToastItem;

const enterDown = keyframes`
  from { transform: translateY(-8px); opacity: 0; }
  to   { transform: translateY(0); opacity: 1; }
`;
const enterUp = keyframes`
  from { transform: translateY(8px); opacity: 0; }
  to   { transform: translateY(0); opacity: 1; }
`;

const progressShrink = keyframes`
  from { width: 100%; }
  to { width: 0%; }
`;

const Container = styled.div<{
  enterFromTop: boolean;
  type: ToastType;
}>`
  overflow: hidden;
  position: relative;
  width: 420px;
  padding: 12px;
  border: 1px solid;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgb(0 0 0 / 25%);

  display: flex;
  gap: 8px;

  background-color: ${({ theme }) => theme.colors.white};

  border-color: ${({ type }) => theme.colors[type]};

  pointer-events: auto;
  will-change: transform, opacity;

  @media (prefers-reduced-motion: no-preference) {
    animation: ${({ enterFromTop }) => (enterFromTop ? enterDown : enterUp)}
      160ms ease-out both;
  }
`;

const ToastIcon = styled.img``;

const Content = styled.div`
  font-size: 14px;
  line-height: 1.45;
`;

const ProgressBar = styled.div<{ type: ToastType; duration: number }>`
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 4px;

  background-color: ${({ type }) => theme.colors[type]};

  animation: ${progressShrink} ${({ duration }) => duration}s linear forwards;
`;
