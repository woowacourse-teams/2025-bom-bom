import { useEffect, useRef } from 'react';

interface useScrollLockParams {
  scrollLock: boolean;
  isOpen: boolean;
}

export const useScrollLock = ({ scrollLock, isOpen }: useScrollLockParams) => {
  const initialScrollStatus = useRef('');

  useEffect(() => {
    if (!scrollLock) return;

    if (isOpen) {
      initialScrollStatus.current = document.body.style.overflow;
      document.body.style.overflow = 'hidden';
    }

    return () => {
      document.body.style.overflow = initialScrollStatus.current;
    };
  }, [isOpen, scrollLock]);
};
