import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';
import { useEffect } from 'react';
import { ToastData, ToastType } from './toast.type';
import { hideToast } from './toastActions';

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
    <Container
      duration={duration / 1000}
      enterFromTop={isTop}
      type={toast.type}
    >
      <Badge aria-hidden>{toast.type === 'success' ? '✓' : '⚠️'}</Badge>
      <Content>{toast.message}</Content>
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
  duration: number;
  type: ToastType;
}>`
  width: 420px;
  padding: 12px;
  border: 1px solid #1f2937;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgb(0 0 0 / 25%);

  display: flex;
  gap: 8px;

  background: #111827;
  color: #f9fafb;

  pointer-events: auto;
  will-change: transform, opacity;

  @media (prefers-reduced-motion: no-preference) {
    animation: ${({ enterFromTop }) => (enterFromTop ? enterDown : enterUp)}
      160ms ease-out both;
  }

  &::after {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 3px;

    background-color: currentcolor;

    animation: ${progressShrink} ${({ duration }) => duration}s linear forwards;

    content: '';
  }

  ${({ type }) =>
    type === 'success'
      ? `
      background-color: #064e3b;
      border-color: #065f46;
      `
      : `
      background-color: #450a0a;
      border-color: #7f1d1d;
      `}
`;

const Badge = styled.span`
  width: 24px;
  height: 24px;
  border-radius: 999px;

  display: grid;
  flex: 0 0 auto;

  background: rgb(255 255 255 / 10%);
  font-size: 14px;

  place-items: center;
`;

const Content = styled.div`
  font-size: 14px;
  line-height: 1.45;
`;
