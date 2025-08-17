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
  width: min(720px, 92vw);
  height: 90vh;
  max-height: min(720px, 90vh);
  padding: 36px 52px;
  border: 0;
  border-radius: 12px;

  display: flex;
  flex-direction: column;
  align-items: center;

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

  display: flex;
  align-items: center;
  justify-content: center;

  background: none;
`;

const StyledCloseIcon = styled(CloseIcon)``;

const ContentWrapper = styled.div`
  height: 100%;
  min-height: 0;
  margin-right: -24px;
  padding-right: 24px;

  display: flex;
  flex-direction: column;

  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
`;
