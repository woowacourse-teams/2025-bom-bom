import { useEffect, useMemo } from 'react';

interface useScrollLockParams {
  scrollLock: boolean;
  isOpen: boolean;
}

export const useScrollLock = ({ scrollLock, isOpen }: useScrollLockParams) => {
  const bodyScrollStatus = useMemo(() => document.body.style.overflow, []);

  useEffect(() => {
    if (!scrollLock) return;

    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = bodyScrollStatus;
    }

    return () => {
      document.body.style.overflow = bodyScrollStatus;
    };
  }, [isOpen, scrollLock, bodyScrollStatus]);
};
