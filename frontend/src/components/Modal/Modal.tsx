import styled from '@emotion/styled';
import { MouseEvent, PropsWithChildren, RefObject } from 'react';
import { theme } from '@/styles/theme';
import CloseIcon from '#/assets/close.svg';

interface UseModalParams extends PropsWithChildren {
  modalRef: RefObject<HTMLDialogElement | null>;
  closeModal: () => void;
  clickOutsideModal: (event: MouseEvent<HTMLDialogElement>) => void;
  defaultScroll?: boolean;
}

const Modal = ({
  modalRef,
  closeModal,
  clickOutsideModal,
  defaultScroll = true,
  children,
}: UseModalParams) => {
  return (
    <Container ref={modalRef} onClick={clickOutsideModal}>
      <CloseButton type="button" onClick={closeModal}>
        <StyledCloseIcon width={36} height={36} fill={theme.colors.black} />
      </CloseButton>
      <ContentWrapper defaultScroll={defaultScroll}>{children}</ContentWrapper>
    </Container>
  );
};

export default Modal;

const Container = styled.dialog`
  overflow: hidden;
  position: relative;
  width: min(720px, 92vw);
  max-height: min(720px, 80vh);
  padding: 32px 52px;
  border: 0;
  border-radius: 12px;

  display: flex;
  flex-direction: column;

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

const ContentWrapper = styled.div<{ defaultScroll: boolean }>`
  min-height: 0;
  flex: 1 1 auto;

  ${({ defaultScroll }) =>
    defaultScroll &&
    `
    -webkit-overflow-scrolling: touch;
    overflow-y: auto;
    overscroll-behavior: contain;
  `}
`;
