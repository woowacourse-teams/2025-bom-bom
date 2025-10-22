import styled from '@emotion/styled';
import Button from '../Button/Button';
import Modal from '../Modal/Modal';
import type { Ref } from 'react';
import logo from '#/assets/avif/logo.avif';

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
            <LogoImage src={logo} alt="봄봄 로고" />
          </LogoSection>
          <TextWrapper>
            <MainText>앱에서는 1초만에!</MainText>
            <SubText>간편하게 로그인하기</SubText>
          </TextWrapper>
        </ContentWrapper>
        <ButtonSection>
          <StyledPrimaryButton
            text="편리한 앱으로 보기"
            onClick={onInstallClick}
          />
          <StyledTextButton
            text="모바일 웹으로 볼래요"
            onClick={onLaterClick}
          />
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

const StyledPrimaryButton = styled(Button)`
  width: 100%;
  height: 56px;
  border-radius: 12px;

  font: ${({ theme }) => theme.fonts.heading5};
`;

const StyledTextButton = styled(Button)`
  width: 100%;
  height: 48px;
  border: none;

  background: transparent;
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body1};

  transition: color 0.2s ease;
`;
