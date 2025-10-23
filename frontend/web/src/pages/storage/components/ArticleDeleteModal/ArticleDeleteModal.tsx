import styled from '@emotion/styled';
import Button from '@/components/Button/Button';
import Modal from '@/components/Modal/Modal';
import { useDevice } from '@/hooks/useDevice';

interface ArticleDeleteModalProps {
  modalRef: (node: HTMLDivElement) => void;
  isOpen: boolean;
  closeModal: () => void;
  onDelete: () => void;
  hasBookmarkedArticles?: boolean;
}

const ArticleDeleteModal = ({
  modalRef,
  isOpen,
  closeModal,
  onDelete,
  hasBookmarkedArticles = false,
}: ArticleDeleteModalProps) => {
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
        <ModalTitle isMobile={isMobile}>
          선택한 아티클을 삭제하시겠습니까?
        </ModalTitle>

        {hasBookmarkedArticles && (
          <HighlightedWarning isMobile={isMobile}>
            선택한 아티클 중 북마크된 항목이 포함되어 있습니다.
          </HighlightedWarning>
        )}

        <ModalDescription isMobile={isMobile}>
          {hasBookmarkedArticles
            ? `삭제하면 복구할 수 없으며, 북마크도 함께 사라집니다. \n 정말 삭제를 진행하시겠습니까?`
            : `삭제된 아티클은 복구할 수 없습니다. \n 삭제를 진행하시겠습니까?`}
        </ModalDescription>

        <ModalButtonGroup>
          <ModalButton
            isMobile={isMobile}
            text="삭제"
            onClick={() => {
              onDelete();
              closeModal();
            }}
          />
          <ModalButton
            isMobile={isMobile}
            variant="outlined"
            text="취소"
            onClick={closeModal}
          />
        </ModalButtonGroup>
      </Container>
    </Modal>
  );
};

export default ArticleDeleteModal;

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
    isMobile ? theme.fonts.body3 : theme.fonts.body2};
  white-space: pre-wrap;
`;

const HighlightedWarning = styled.div<{ isMobile: boolean }>`
  width: 100%;
  padding: 12px;
  border-radius: 8px;

  background-color: ${({ theme }) => `${theme.colors.error}20`};
  color: ${({ theme }) => theme.colors.error};
  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.body3 : theme.fonts.body2};
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
