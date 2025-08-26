/** @jsxImportSource @emotion/react */
import styled from '@emotion/styled';
import { ToastPosition } from './toast.type';
import ToastItem from './ToastItem';
import { useToasts } from './useToasts';

type Props = {
  limit?: number;
  duration?: number;
  position?: ToastPosition;
};

export const Toast = ({
  limit = 3,
  duration = 1000,
  position = 'top-right',
}: Props) => {
  const { toasts } = useToasts(limit);

  return (
    <Layer role="region" aria-label="Notifications">
      <Stack $position={position}>
        {toasts.map((toast) => (
          <ToastItem
            key={toast.id}
            toast={toast}
            isTop={position.startsWith('top')}
            duration={duration}
          />
        ))}
      </Stack>
    </Layer>
  );
};

const Layer = styled.div`
  position: fixed;
  z-index: 9999;

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

const Stack = styled.div<{ $position: ToastPosition }>`
  position: fixed;

  display: flex;
  ${({ $position }) => mapPosition($position)}
  gap: 12px;
  flex-direction: column;

  pointer-events: none;
`;
