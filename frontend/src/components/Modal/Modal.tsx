import styled from '@emotion/styled';
import { MouseEvent, RefObject } from 'react';
import { theme } from '@/styles/theme';
import CloseIcon from '#/assets/close.svg';

interface UseModalParams {
  modalRef: RefObject<HTMLDialogElement | null>;
  closeModal: () => void;
  clickOutsideModal: (event: MouseEvent<HTMLDialogElement>) => void;
}

const Modal = ({ modalRef, closeModal, clickOutsideModal }: UseModalParams) => {
  return (
    <Container ref={modalRef} onClick={clickOutsideModal}>
      <CloseButton type="button" onClick={closeModal}>
        <StyledCloseIcon fill={theme.colors.black} />
      </CloseButton>
      모달 컨텐츠
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
