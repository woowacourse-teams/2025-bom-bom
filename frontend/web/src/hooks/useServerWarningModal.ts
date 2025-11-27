import { useEffect, useRef, useState } from 'react';

export function useServerWarningModal() {
  const modalRef = useRef<HTMLDivElement>(null);
  const modalClosed = useRef(false);

  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    setShowModal(true);
  }, []);

  const handleCloseModal = () => {
    modalClosed.current = true;
    setShowModal(false);
  };

  return {
    showModal,
    modalClosed,
    handleCloseModal,
    modalRef,
  };
}
