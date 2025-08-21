import { useCallback, useEffect } from 'react';

const useKeydownEscape = (callback: VoidFunction | null) => {
  const keydownEscapeHandler = useCallback(
    (event: KeyboardEvent) => {
      if (!callback) return;

      if (event.key === 'Escape') {
        event.stopPropagation();
        callback();
      }
    },
    [callback],
  );

  useEffect(() => {
    document.addEventListener('keydown', keydownEscapeHandler);

    return () => {
      document.removeEventListener('keydown', keydownEscapeHandler);
    };
  }, [keydownEscapeHandler]);
};

export default useKeydownEscape;
