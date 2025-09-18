import { useCallback, useState } from 'react';
import {
  ToolbarPosition,
  FloatingToolbarMode,
} from './../components/FloatingToolbar/FloatingToolbar.types';

export const useFloatingToolbarState = () => {
  const [open, setOpen] = useState(false);
  const [position, setPosition] = useState<ToolbarPosition>({ x: 0, y: 0 });
  const [mode, setMode] = useState<FloatingToolbarMode>('new');

  const showToolbar = useCallback(
    ({
      position,
      mode,
    }: {
      position: ToolbarPosition;
      mode: FloatingToolbarMode;
    }) => {
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
