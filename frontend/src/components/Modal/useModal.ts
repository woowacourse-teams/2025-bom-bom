import { useCallback, useState, useEffect, useMemo } from 'react';
import { useClickOutsideRef } from '@/hooks/useClickOutsideRef';

const useModal = () => {
  const [isOpen, setIsOpen] = useState(false);
  const bodyScrollStatus = useMemo(() => document.body.style.overflow, []);

  const openModal = useCallback(() => {
    setIsOpen(true);
  }, []);

  const closeModal = useCallback(() => {
    setIsOpen(false);
  }, []);

  const modalRef = useClickOutsideRef<HTMLDivElement>(closeModal);

  const keydownESCModal = useCallback(
    (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        closeModal();
      }
    },
    [closeModal],
  );

  useEffect(() => {
    window.addEventListener('keydown', keydownESCModal);

    return () => window.removeEventListener('keydown', keydownESCModal);
  }, [keydownESCModal]);

  const toggleScrollLock = useCallback(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = bodyScrollStatus;
    }
  }, [isOpen, bodyScrollStatus]);

  useEffect(() => {
    toggleScrollLock();
  }, [isOpen, toggleScrollLock]);

  return {
    modalRef,
    openModal,
    closeModal,
    isOpen,
  };
};

export default useModal;
