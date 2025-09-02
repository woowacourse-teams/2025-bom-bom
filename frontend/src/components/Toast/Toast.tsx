import styled from '@emotion/styled';
import {
  DEFAULT_DURATION,
  DEFAULT_LIMIT,
  DEFAULT_POSITION,
} from './Toast.constants';
import { ToastPosition } from './Toast.types';
import ToastItem from './ToastItem';
import { useToasts } from './useToasts';

interface ToastProps {
  limit?: number;
  duration?: number;
  position?: ToastPosition;
}

const Toast = ({
  limit = DEFAULT_LIMIT,
  duration = DEFAULT_DURATION,
  position = DEFAULT_POSITION,
}: ToastProps) => {
  const { toasts } = useToasts(limit);

  return (
    <Container role="region" aria-label="Notifications">
      <StackWrapper position={position}>
        {toasts.map((toast) => (
          <ToastItem
            key={toast.id}
            toast={toast}
            isTop={position.startsWith('top')}
            duration={duration}
          />
        ))}
      </StackWrapper>
    </Container>
  );
};

export default Toast;

const Container = styled.div`
  position: fixed;
  z-index: ${({ theme }) => theme.zIndex.toast};

  inset: 0;
  pointer-events: none;
`;

const mapPosition = (p: ToastPosition) => {
  const base = {
    top: 'auto',
    bottom: 'auto',
    left: 'auto',
    right: 'auto',
    transform: '',
  };
  if (p.startsWith('top')) base.top = '16px';
  if (p.startsWith('bottom')) base.bottom = '16px';
  if (p.endsWith('left')) base.left = '16px';
  if (p.endsWith('right')) base.right = '16px';
  if (p.endsWith('center')) {
    base.left = '50%';
    base.transform = 'translateX(-50%)';
  }
  return base;
};

const StackWrapper = styled.div<{ position: ToastPosition }>`
  position: fixed;

  display: flex;
  ${({ position }) => mapPosition(position)}
  gap: 12px;
  flex-direction: column;

  pointer-events: none;
`;
