import { useEffect, useRef } from 'react';

export function useClickOutsideRef<T extends HTMLElement = HTMLElement>(
  callback: (() => void) | null,
) {
  const ref = useRef<T>(null);

  useEffect(() => {
    if (!callback) return;

    const listener = (event: Event) => {
      if (!ref.current || ref.current.contains(event.target as Node)) {
        return;
      }
      callback();
    };

    document.addEventListener('click', listener, true);
    return () => {
      document.removeEventListener('click', listener, true);
    };
  }, [callback]);

  return ref;
}
