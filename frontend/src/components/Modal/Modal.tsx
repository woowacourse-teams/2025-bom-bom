import styled from '@emotion/styled';
import { PropsWithChildren, RefObject } from 'react';
import { theme } from '@/styles/theme';
import CloseIcon from '#/assets/close.svg';

type PositionType = 'center' | 'bottom';

interface UseModalParams extends PropsWithChildren {
  modalRef: RefObject<HTMLDivElement | null>;
  closeModal: () => void;
  isOpen: boolean;
  position?: PositionType;
  showCloseButton?: boolean;
}

const Modal = ({
  modalRef,
  closeModal,
  position = 'center',
  showCloseButton = true,
  isOpen,
  children,
}: UseModalParams) => {
  const isBottom = position === 'bottom';

  if (!isOpen) return null;

  return (
    <Backdrop>
      <Container
        role="dialog"
        aria-modal="true"
        ref={modalRef}
        isBottom={isBottom}
      >
        {showCloseButton && (
          <CloseButton type="button" onClick={closeModal}>
            <CloseIcon width={36} height={36} fill={theme.colors.black} />
          </CloseButton>
        )}
        <ContentWrapper isBottom={isBottom}>{children}</ContentWrapper>
      </Container>
    </Backdrop>
  );
};

export default Modal;

const Backdrop = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;
  width: 100%;
  height: 100%;

  display: flex;
  align-items: center;
  justify-content: center;

  background: rgb(0 0 0 / 30%);

  backdrop-filter: blur(2px);
`;

const Container = styled.div<{ isBottom: boolean }>`
  overflow: hidden;
  position: relative;
  width: ${({ isBottom }) => (isBottom ? '100%' : 'min(720px, 92vw)')};
  height: 100%;
  max-height: ${({ isBottom }) => (isBottom ? '45vh' : '90vh')};
  border-radius: 12px;

  display: flex;
  flex-direction: column;

  background: ${({ theme }) => theme.colors.white};

  ${({ isBottom }) =>
    isBottom &&
    `
      align-self: flex-end;
      border-bottom-left-radius: 0;
      border-bottom-right-radius: 0;
`}
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

const ContentWrapper = styled.div<{ isBottom: boolean }>`
  height: 100%;
  min-height: 0;
  padding: ${({ isBottom }) => (isBottom ? '32px' : '36px 52px')};

  display: flex;
  flex-direction: column;
  align-items: center;

  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
`;
