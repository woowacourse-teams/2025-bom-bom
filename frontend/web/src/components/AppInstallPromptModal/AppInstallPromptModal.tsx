import styled from '@emotion/styled';
import Modal from '../Modal/Modal';
import type { Ref } from 'react';

interface AppInstallPromptModalProps {
  modalRef: Ref<HTMLDivElement | null>;
  isOpen: boolean;
  closeModal: () => void;
  onInstallClick: () => void;
  onLaterClick: () => void;
}

const AppInstallPromptModal = ({
  modalRef,
  isOpen,
  closeModal,
  onInstallClick,
  onLaterClick,
}: AppInstallPromptModalProps) => {
  return (
    <Modal
      modalRef={modalRef}
      isOpen={isOpen}
      closeModal={closeModal}
      position="bottom"
      showCloseButton={false}
      showBackdrop={true}
    >
      <Container>
        <ContentWrapper>
          <LogoSection>
            <LogoImage src="/assets/avif/logo.avif" alt="봄봄 로고" />
          </LogoSection>
          <TextWrapper>
            <MainText>앱에서는 1초만에!</MainText>
            <SubText>간편하게 로그인하기</SubText>
          </TextWrapper>
        </ContentWrapper>
        <ButtonSection>
          <InstallButton onClick={onInstallClick}>
            편리한 앱으로 보기
          </InstallButton>
          <LaterButton onClick={onLaterClick}>모바일 웹으로 볼래요</LaterButton>
        </ButtonSection>
      </Container>
    </Modal>
  );
};

export default AppInstallPromptModal;

const Container = styled.div`
  width: 100%;

  display: flex;
  gap: 24px;
  flex-direction: column;
`;

const ContentWrapper = styled.div`
  display: flex;
  gap: 20px;
  align-items: center;
`;

const LogoSection = styled.div`
  overflow: hidden;
  width: 96px;
  height: 96px;
  border-radius: 24px;

  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;

  background: ${({ theme }) => theme.colors.primary};
`;

const LogoImage = styled.img`
  width: 72px;
  height: 72px;

  object-fit: contain;
`;

const TextWrapper = styled.div`
  display: flex;
  gap: 4px;
  flex: 1;
  flex-direction: column;
`;

const MainText = styled.h2`
  margin: 0;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading4};
`;

const SubText = styled.p`
  margin: 0;

  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme }) => theme.fonts.heading5};
`;

const ButtonSection = styled.div`
  display: flex;
  gap: 12px;
  flex-direction: column;
`;

const InstallButton = styled.button`
  width: 100%;
  height: 56px;
  border: none;
  border-radius: 12px;

  background: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.heading5};

  cursor: pointer;
  transition: background 0.2s ease;

  &:active {
    transform: scale(0.98);
  }
`;

const LaterButton = styled.button`
  width: 100%;
  height: 48px;
  border: none;

  background: transparent;
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body1};

  cursor: pointer;
  transition: color 0.2s ease;

  &:hover {
    color: ${({ theme }) => theme.colors.textPrimary};
  }

  &:active {
    transform: scale(0.98);
  }
`;
