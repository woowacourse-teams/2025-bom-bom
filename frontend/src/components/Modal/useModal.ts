import {
  MouseEvent,
  useRef,
  useCallback,
  useState,
  useEffect,
  useMemo,
} from 'react';

const useModal = () => {
  const [isOpen, setIsOpen] = useState(false);
  const modalRef = useRef<HTMLDialogElement>(null);
  const bodyScrollStatus = useMemo(() => document.body.style.overflow, []);

  const openModal = useCallback(() => {
    modalRef.current?.showModal();
    setIsOpen(true);
  }, []);

  const closeModal = useCallback(() => {
    modalRef.current?.close();
    setIsOpen(false);
  }, []);

  const clickOutsideModal = useCallback(
    (event: MouseEvent<HTMLDialogElement>) => {
      const { target, currentTarget } = event;
      if (target === currentTarget) {
        closeModal();
      }
    },
    [closeModal],
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

  return {
    modalRef,
    openModal,
    closeModal,
    clickOutsideModal,
  };
};

export default useModal;
