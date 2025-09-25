import { useCallback, useMemo, useState } from 'react';
import type { FloatingToolbarMode } from './../components/FloatingToolbar/FloatingToolbar.types';
import type { Position } from '@/types/position';

const DEFAULT_POSITION: Position = { x: 0, y: 0 };
const DEFAULT_MODE: FloatingToolbarMode = 'none';

export const useFloatingToolbarState = () => {
  const [position, setPosition] = useState<Position>(DEFAULT_POSITION);
  const [mode, setMode] = useState<FloatingToolbarMode>(DEFAULT_MODE);
  const opened = useMemo(() => mode !== 'none', [mode]);

  const showToolbar = useCallback(
    ({ position, mode }: { position: Position; mode: FloatingToolbarMode }) => {
      setPosition(position);
      setMode(mode);
    },
    [],
  );

  const hideToolbar = useCallback(() => {
    setPosition(DEFAULT_POSITION);
    setMode(DEFAULT_MODE);
  }, []);

  return { opened, position, mode, showToolbar, hideToolbar };
};
