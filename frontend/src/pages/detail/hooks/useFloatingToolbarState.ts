import { useCallback, useState } from 'react';
import { FloatingToolbarMode } from './../components/FloatingToolbar/FloatingToolbar.types';
import { Position } from '@/types/position';

const DEFAULT_POSITION: Position = { x: 0, y: 0 };
const DEFAULT_MODE: FloatingToolbarMode = 'new';

export const useFloatingToolbarState = () => {
  const [open, setOpen] = useState(false);
  const [position, setPosition] = useState<Position>(DEFAULT_POSITION);
  const [mode, setMode] = useState<FloatingToolbarMode>(DEFAULT_MODE);

  const showToolbar = useCallback(
    ({ position, mode }: { position: Position; mode: FloatingToolbarMode }) => {
      setPosition(position);
      setMode(mode);
      setOpen(true);
    },
    [],
  );

  const hideToolbar = useCallback(() => {
    setPosition(DEFAULT_POSITION);
    setMode(DEFAULT_MODE);
    setOpen(false);
  }, []);

  return { open, position, mode, showToolbar, hideToolbar };
};
