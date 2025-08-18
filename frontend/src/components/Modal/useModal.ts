import {
  MouseEvent,
  useRef,
  useCallback,
  useState,
  useEffect,
  useMemo,
} from 'react';
import useFocusTrap from '@/hooks/useFocusTrap';

const useModal = () => {
  const [isOpen, setIsOpen] = useState(false);
  const modalRef = useRef<HTMLDivElement>(null);
  const { setFocusRef, keydownFocusTrapTab } = useFocusTrap();
  const bodyScrollStatus = useMemo(() => document.body.style.overflow, []);

  const openModal = useCallback(() => {
    setIsOpen(true);
  }, []);

  const closeModal = useCallback(() => {
    setIsOpen(false);
  }, []);

  const clickOutsideModal = useCallback(
    (event: MouseEvent<HTMLDivElement>) => {
      const { target, currentTarget } = event;
      if (target === currentTarget) {
        closeModal();
      }
    },
    [closeModal],
  );

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

  useEffect(() => {
    const modal = modalRef.current;
    if (!modal || !isOpen) {
      return;
    }

    setFocusRef(modal);
  }, [isOpen, setFocusRef]);

  return {
    modalRef,
    openModal,
    closeModal,
    clickOutsideModal,
    keydownFocusTrapTab,
    isOpen,
  };
};

export default useModal;
