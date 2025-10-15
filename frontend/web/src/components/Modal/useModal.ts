import { useCallback, useEffect, useRef, useState } from 'react';
import { useClickOutsideRef } from '@/hooks/useClickOutsideRef';
import useFocusTrap from '@/hooks/useFocusTrap';
import useKeydownEscape from '@/hooks/useKeydownEscape';
import { useScrollLock } from '@/hooks/useScrollLock';
import { compoundRefs } from '@/utils/element';

interface UseModalOptions {
  scrollLock?: boolean;
  onOpen?: () => void;
  onClose?: () => void;
}

const useModal = (options: UseModalOptions = {}) => {
  const { scrollLock = true, onOpen, onClose } = options;
  const onOpenRef = useRef(onOpen);
  const onCloseRef = useRef(onClose);
  const [isOpen, setIsOpen] = useState(false);

  const openModal = useCallback(() => {
    onOpenRef.current?.();
    setIsOpen(true);
  }, []);

  const closeModal = useCallback(() => {
    onCloseRef.current?.();
    setIsOpen(false);
  }, []);

  const clickOutsideRef = useClickOutsideRef<HTMLDivElement>(closeModal);
  const focusTrapRef = useFocusTrap<HTMLDivElement>({
    isActive: isOpen,
  });

  const modalRef = compoundRefs<HTMLDivElement>(clickOutsideRef, focusTrapRef);

  useScrollLock({ scrollLock, isOpen });
  useKeydownEscape(isOpen ? closeModal : null);

  useEffect(() => {
    onOpenRef.current = onOpen;
    onCloseRef.current = onClose;
  });

  return {
    modalRef,
    openModal,
    closeModal,
    isOpen,
  };
};

export default useModal;
