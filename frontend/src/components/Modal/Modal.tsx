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
        <StyledCloseIcon width={36} height={36} fill={theme.colors.black} />
      </CloseButton>
      <ContentWrapper>{children}</ContentWrapper>
    </Container>
  );
};

export default Modal;

const Container = styled.dialog`
  overflow: hidden;
  position: relative;
  width: min(680px, 92vw);
  max-height: min(80dvh, 720px);
  padding: 32px;
  border: 0;
  border-radius: 12px;

  display: flex;

  &::backdrop {
    background: rgb(0 0 0 / 30%);
    backdrop-filter: blur(2px);
  }
`;

const CloseButton = styled.button`
  position: absolute;
  top: 20px;
  right: 20px;
  border: none;

  background: none;

  place-items: center;
`;

const StyledCloseIcon = styled(CloseIcon)``;

const ContentWrapper = styled.div`
  min-height: 0;

  flex: 1 1 auto;

  -webkit-overflow-scrolling: touch;
  overflow-y: auto;
  overscroll-behavior: contain;
`;
