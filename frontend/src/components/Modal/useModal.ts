import { useCallback, useState, useEffect, useMemo } from 'react';
import { useClickOutsideRef } from '@/hooks/useClickOutsideRef';
import useFocusTrap from '@/hooks/useFocusTrap';
import useKeydownEscape from '@/hooks/useKeydownEscape';

const useModal = () => {
  const [isOpen, setIsOpen] = useState(false);
  const bodyScrollStatus = useMemo(() => document.body.style.overflow, []);

  const openModal = useCallback(() => {
    setIsOpen(true);
  }, []);

  const closeModal = useCallback(() => {
    setIsOpen(false);
  }, []);

  const clickOutsideRef = useClickOutsideRef<HTMLDivElement>(closeModal);
  const { containerRef: focusTrapRef } = useFocusTrap<HTMLDivElement>({
    isActive: isOpen,
  });

  const modalRef = useCallback(
    (node: HTMLDivElement | null) => {
      clickOutsideRef.current = node;
      focusTrapRef.current = node;
    },
    [clickOutsideRef, focusTrapRef],
  );

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

  useKeydownEscape(isOpen ? closeModal : null);

  return {
    modalRef,
    openModal,
    closeModal,
    isOpen,
  };
};

export default useModal;
