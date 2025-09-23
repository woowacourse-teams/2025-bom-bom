import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';
import { useEffect } from 'react';
import { hideToast } from './utils/toastActions';
import type { ToastData, ToastVariant } from './Toast.types';
import type { FunctionComponent, SVGProps } from 'react';
import ErrorIcon from '#/assets/cancel-circle.svg';
import SuccessIcon from '#/assets/check-circle.svg';
import InfoIcon from '#/assets/info-circle.svg';

const iconMap: Record<
  ToastVariant,
  FunctionComponent<SVGProps<SVGSVGElement>>
> = {
  error: ErrorIcon,
  info: InfoIcon,
  success: SuccessIcon,
};

interface ToastItemProps {
  toast: ToastData;
  isTop: boolean;
  duration: number;
}

const ToastItem = ({ toast, isTop, duration }: ToastItemProps) => {
  const ToastIcon = iconMap[toast.variant];

  useEffect(() => {
    const timerId = setTimeout(() => {
      if (toast.id) hideToast(toast.id);
    }, duration);
    return () => {
      clearTimeout(timerId);
    };
  }, [duration, toast.id]);

  return (
    <Container enterFromTop={isTop} variant={toast.variant}>
      <ToastIcon />
      <Content>{toast.message}</Content>
      <ProgressBar variant={toast.variant} duration={duration / 1000} />
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
  variant: ToastVariant;
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

  border-color: ${({ theme, variant }) => theme.colors[variant]};

  pointer-events: auto;
  will-change: transform, opacity;

  @media (prefers-reduced-motion: no-preference) {
    animation: ${({ enterFromTop }) => (enterFromTop ? enterDown : enterUp)}
      160ms ease-out both;
  }
`;

const Content = styled.div`
  font: ${({ theme }) => theme.fonts.body2};
`;

const ProgressBar = styled.div<{ variant: ToastVariant; duration: number }>`
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 4px;

  background-color: ${({ theme, variant }) => theme.colors[variant]};

  animation: ${progressShrink} ${({ duration }) => duration}s linear forwards;
`;
