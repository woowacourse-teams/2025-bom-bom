import styled from '@emotion/styled';
import Button from '@/components/Button/Button';
import Modal from '@/components/Modal/Modal';
import { useDevice } from '@/hooks/useDevice';
import type { RefObject } from 'react';

interface ServerWarningModalProps {
  modalRef: RefObject<HTMLDivElement | null>;
  isOpen: boolean;
  closeModal: () => void;
}

const ServerWarningModal = ({
  modalRef,
  isOpen,
  closeModal,
}: ServerWarningModalProps) => {
  const device = useDevice();
  const isMobile = device === 'mobile';

  return (
    <Modal
      modalRef={modalRef}
      isOpen={isOpen}
      closeModal={closeModal}
      showCloseButton={false}
    >
      <Container isMobile={isMobile}>
        <ModalTitle isMobile={isMobile}>서비스 점검 안내 ⚠️</ModalTitle>
        <HighlightedWarning isMobile={isMobile}>
          점검 시간: 27일(목) 오후 4시 ~ 28일(금) 오전 6시
        </HighlightedWarning>
        <ModalDescription isMobile={isMobile}>
          서버 이전 작업이 예정되어 있습니다. 더 나은 서비스를 위한 필수
          작업으로, 상황에 따라 점검 시간이 달라질 수 있습니다.
        </ModalDescription>

        <ModalButtonGroup>
          <ModalButton
            isMobile={isMobile}
            text="확인"
            onClick={() => {
              closeModal();
            }}
          />
        </ModalButtonGroup>
      </Container>
    </Modal>
  );
};

export default ServerWarningModal;

const Container = styled.div<{ isMobile: boolean }>`
  width: ${({ isMobile }) => (isMobile ? '320px' : '440px')};

  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '12px' : '20px')};
  flex-direction: column;
  align-items: center;
  justify-content: center;

  text-align: center;
`;

const ModalTitle = styled.h2<{ isMobile: boolean }>`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.heading5 : theme.fonts.heading4};
`;

const ModalDescription = styled.p<{ isMobile: boolean }>`
  margin: 0;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.body2 : theme.fonts.body1};
  white-space: pre-wrap;
`;

const HighlightedWarning = styled.div<{ isMobile: boolean }>`
  width: 100%;
  padding: 12px;
  border-radius: 8px;

  background-color: ${({ theme }) => `${theme.colors.error}20`};
  color: ${({ theme }) => theme.colors.error};
  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.body2 : theme.fonts.heading5};
`;

const ModalButtonGroup = styled.div`
  display: flex;
  gap: 12px;
  justify-content: center;
`;

const ModalButton = styled(Button)<{ isMobile: boolean }>`
  width: ${({ isMobile }) => (isMobile ? '80px' : '160px')};
  height: 48px;
  border-radius: 8px;

  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.body3 : theme.fonts.body2};
`;
