import { MouseEvent, useRef, useCallback } from 'react';

const useModal = () => {
  const modalRef = useRef<HTMLDialogElement>(null);

  const openModal = useCallback(() => {
    modalRef.current?.showModal();
  }, []);

  const closeModal = useCallback(() => {
    modalRef.current?.close();
  }, []);

  const clickOutsideModal = useCallback(
    (event: MouseEvent<HTMLDialogElement>) => {
      const { target, currentTarget } = event;
      if (target === currentTarget) {
        closeModal();
      }
    },
    [],
  );

  return {
    modalRef,
    openModal,
    closeModal,
    clickOutsideModal,
  };
};

export default useModal;
