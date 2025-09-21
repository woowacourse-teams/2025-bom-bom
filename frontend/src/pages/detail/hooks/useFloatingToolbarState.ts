import { useCallback, useState } from 'react';
import { FloatingToolbarMode } from './../components/FloatingToolbar/FloatingToolbar.types';
import { Position } from '@/types/position';

export const useFloatingToolbarState = () => {
  const [open, setOpen] = useState(false);
  const [position, setPosition] = useState<Position>({ x: 0, y: 0 });
  const [mode, setMode] = useState<FloatingToolbarMode>('new');

  const showToolbar = useCallback(
    ({ position, mode }: { position: Position; mode: FloatingToolbarMode }) => {
      setPosition(position);
      setMode(mode);
      setOpen(true);
    },
    [],
  );

  const hideToolbar = useCallback(() => {
    setOpen(false);
  }, []);

  return { open, position, mode, showToolbar, hideToolbar };
};
