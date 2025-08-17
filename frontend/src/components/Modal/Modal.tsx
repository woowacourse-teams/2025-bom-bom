import styled from '@emotion/styled';
import { MouseEvent, PropsWithChildren, RefObject } from 'react';
import { theme } from '@/styles/theme';
import CloseIcon from '#/assets/close.svg';

type PositionType = 'center' | 'bottom';

interface UseModalParams extends PropsWithChildren {
  modalRef: RefObject<HTMLDialogElement | null>;
  closeModal: () => void;
  clickOutsideModal: (event: MouseEvent<HTMLDialogElement>) => void;
  position?: PositionType;
  showCloseButton?: boolean;
}

const Modal = ({
  modalRef,
  closeModal,
  clickOutsideModal,
  position = 'center',
  showCloseButton = true,
  children,
}: UseModalParams) => {
  return (
    <Container ref={modalRef} onClick={clickOutsideModal} position={position}>
      {showCloseButton && (
        <CloseButton type="button" onClick={closeModal}>
          <StyledCloseIcon width={36} height={36} fill={theme.colors.black} />
        </CloseButton>
      )}
      <ContentWrapper>{children}</ContentWrapper>
    </Container>
  );
};

export default Modal;

const Container = styled.dialog<{ position: PositionType }>`
  overflow: hidden;
  position: fixed;
  width: ${({ position }) =>
    position === 'bottom' ? '100%' : 'min(720px, 92vw)'};
  height: 90vh;
  max-height: min(720px, 90vh);
  padding: 0;
  border: 0;
  border-radius: 12px;

  display: flex;
  flex-direction: column;

  ${({ position }) =>
    position === 'bottom' &&
    `
      height: 60vh;
      max-width: 100%;
      margin: 0;
      margin-top: auto;
      border-bottom-left-radius: 0;
      border-bottom-right-radius: 0;
`}

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
  padding: 36px 52px;

  display: flex;
  flex-direction: column;
  align-items: center;

  box-sizing: border-box;

  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
`;
