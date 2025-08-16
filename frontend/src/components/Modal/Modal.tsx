import styled from '@emotion/styled';
import { MouseEvent, PropsWithChildren, RefObject } from 'react';
import { theme } from '@/styles/theme';
import CloseIcon from '#/assets/close.svg';

interface UseModalParams extends PropsWithChildren {
  modalRef: RefObject<HTMLDialogElement | null>;
  closeModal: () => void;
  clickOutsideModal: (event: MouseEvent<HTMLDialogElement>) => void;
}

const Modal = ({
  modalRef,
  closeModal,
  clickOutsideModal,
  children,
}: UseModalParams) => {
  return (
    <Container ref={modalRef} onClick={clickOutsideModal}>
      <CloseButton type="button" onClick={closeModal}>
        <StyledCloseIcon fill={theme.colors.black} />
      </CloseButton>
      {children}
    </Container>
  );
};

export default Modal;

const Container = styled.dialog`
  position: relative;
  padding: 32px;
  border: 0;
  border-radius: 12px;

  &::backdrop {
    background: rgb(0 0 0 / 30%);
    backdrop-filter: blur(2px);
  }
`;

const CloseButton = styled.button`
  position: absolute;
  top: 8px;
  right: 8px;
  border: none;

  background: none;

  place-items: center;
`;

const StyledCloseIcon = styled(CloseIcon)``;
