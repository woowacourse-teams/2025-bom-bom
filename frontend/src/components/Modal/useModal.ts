import { useCallback, useState, useEffect, useMemo } from 'react';
import { useClickOutsideRef } from '@/hooks/useClickOutsideRef';
import useFocusTrap from '@/hooks/useFocusTrap';
import useKeydownEscape from '@/hooks/useKeydownEscape';
import { compoundRefs } from '@/utils/element';

interface UseModalOptions {
  scrollLock?: boolean;
}

const useModal = (options: UseModalOptions = {}) => {
  const { scrollLock = true } = options;
  const [isOpen, setIsOpen] = useState(false);
  const bodyScrollStatus = useMemo(() => document.body.style.overflow, []);

  const openModal = useCallback(() => {
    setIsOpen(true);
  }, []);

  const closeModal = useCallback(() => {
    setIsOpen(false);
  }, []);

  const clickOutsideRef = useClickOutsideRef<HTMLDivElement>(closeModal);
  const focusTrapRef = useFocusTrap<HTMLDivElement>({
    isActive: isOpen,
  });

  const modalRef = compoundRefs<HTMLDivElement>(clickOutsideRef, focusTrapRef);

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

  useKeydownEscape(isOpen ? closeModal : null);

  return {
    modalRef,
    openModal,
    closeModal,
    isOpen,
  };
};

export default useModal;
