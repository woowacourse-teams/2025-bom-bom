import { theme } from '@bombom/shared/theme';
import styled from '@emotion/styled';
import type { PropsWithChildren, Ref } from 'react';
import CloseIcon from '#/assets/svg/close.svg';

type Position = 'center' | 'bottom' | 'dropdown';

interface UseModalParams extends PropsWithChildren {
  modalRef: Ref<HTMLDivElement | null>;
  closeModal: () => void;
  isOpen: boolean;
  position?: Position;
  showCloseButton?: boolean;
  showBackdrop?: boolean;
}

const Modal = ({
  modalRef,
  closeModal,
  position = 'center',
  showCloseButton = true,
  showBackdrop = true,
  isOpen,
  children,
}: UseModalParams) => {
  if (!isOpen) return null;

  return (
    <>
      {showBackdrop && <Backdrop onClick={closeModal} />}
      <Container
        role="dialog"
        aria-modal="true"
        ref={modalRef}
        position={position}
      >
        {showCloseButton && (
          <CloseButton type="button" onClick={closeModal}>
            <CloseIcon width={36} height={36} fill={theme.colors.black} />
          </CloseButton>
        )}
        <ContentWrapper position={position}>{children}</ContentWrapper>
      </Container>
    </>
  );
};

export default Modal;

const Backdrop = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  z-index: ${({ theme }) => theme.zIndex.overlay};
  width: 100%;
  height: 100%;

  display: flex;
  align-items: center;
  justify-content: center;

  background: rgb(0 0 0 / 30%);

  backdrop-filter: blur(2px);
`;

const Container = styled.div<{
  position: Position;
}>`
  position: fixed;
  z-index: ${({ theme }) => theme.zIndex.overlay};

  display: flex;
  flex-direction: column;

  background: ${({ theme }) => theme.colors.white};

  ${({ position }) => containerStyles[position]}
`;

const CloseButton = styled.button`
  position: absolute;
  top: 20px;
  right: 20px;
  border: none;

  display: flex;
  align-items: center;
  justify-content: center;

  background: none;
`;

const ContentWrapper = styled.div<{ position: Position }>`
  min-height: 0;

  display: flex;
  flex-direction: column;

  overflow-y: auto;

  ${({ position }) => contentWrapperStyles[position]}
`;

const containerStyles = {
  dropdown: {
    height: 'auto',
    borderRadius: '8px',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
  },
  bottom: {
    bottom: 0,
    left: 0,
    right: 0,
    width: '100%',
    height: '100%',
    maxHeight: '80vh',
    borderRadius: '12px 12px 0 0',
    boxShadow: 'none',
  },
  center: {
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    maxHeight: '90vh',
    borderRadius: '12px',
    boxShadow: 'none',
  },
};

const contentWrapperStyles = {
  dropdown: {
    height: 'auto',
    padding: '16px',
    alignItems: 'stretch',
  },
  bottom: {
    height: '100%',
    padding: '32px',
    alignItems: 'center',
  },
  center: {
    height: '100%',
    padding: '36px 52px',
    alignItems: 'center',
  },
};
