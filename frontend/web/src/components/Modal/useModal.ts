import { useNavigate } from '@tanstack/react-router';
import { useCallback, useState } from 'react';
import { useClickOutsideRef } from '@/hooks/useClickOutsideRef';
import useFocusTrap from '@/hooks/useFocusTrap';
import useKeydownEscape from '@/hooks/useKeydownEscape';
import { useScrollLock } from '@/hooks/useScrollLock';
import { compoundRefs } from '@/utils/element';

interface UseModalOptions {
  scrollLock?: boolean;
}

const useModal = (options: UseModalOptions = {}) => {
  const navigate = useNavigate();
  const { scrollLock = true } = options;
  const [isOpen, setIsOpen] = useState(false);

  const openModal = useCallback(() => {
    setIsOpen(true);
  }, []);

  const closeModal = useCallback(() => {
    setIsOpen(false);
    navigate({
      to: '.',
      search: () => ({}),
      replace: true,
    });
  }, [navigate]);

  const clickOutsideRef = useClickOutsideRef<HTMLDivElement>(closeModal);
  const focusTrapRef = useFocusTrap<HTMLDivElement>({
    isActive: isOpen,
  });

  const modalRef = compoundRefs<HTMLDivElement>(clickOutsideRef, focusTrapRef);

  useScrollLock({ scrollLock, isOpen });
  useKeydownEscape(isOpen ? closeModal : null);

  return {
    modalRef,
    openModal,
    closeModal,
    isOpen,
  };
};

export default useModal;
