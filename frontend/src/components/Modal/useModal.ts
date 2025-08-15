import { MouseEvent, useRef } from 'react';

const useModal = () => {
  const modalRef = useRef<HTMLDialogElement>(null);

  const openModal = () => {
    modalRef.current?.showModal();
  };

  const closeModal = () => {
    modalRef.current?.close();
  };

  const clickOutsideModal = (event: MouseEvent<HTMLDialogElement>) => {
    const { target, currentTarget } = event;
    if (target === currentTarget) {
      closeModal();
    }
  };

  return {
    modalRef,
    openModal,
    closeModal,
    clickOutsideModal,
  };
};

export default useModal;
