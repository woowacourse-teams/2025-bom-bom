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
            <LogoImage src="/app/assets/images/logo.png" alt="봄봄 로고" />
          </LogoSection>
          <TextSection>
            <MainText>앱에서는 1초만에!</MainText>
            <SubText>간편하게 로그인하기</SubText>
            <RatingSection>
              <StarIcon>⭐</StarIcon>
              <StarIcon>⭐</StarIcon>
              <StarIcon>⭐</StarIcon>
              <StarIcon>⭐</StarIcon>
              <StarIcon>⭐</StarIcon>
              <RatingText>특가, 쿠폰 등 전용 혜택까지</RatingText>
            </RatingSection>
          </TextSection>
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
  flex-direction: column;
  gap: 24px;
`;

const ContentWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 20px;
`;

const LogoSection = styled.div`
  flex-shrink: 0;
  width: 96px;
  height: 96px;
  border-radius: 24px;
  background: ${({ theme }) => theme.colors.primary};
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
`;

const LogoImage = styled.img`
  width: 72px;
  height: 72px;
  object-fit: contain;
`;

const TextSection = styled.div`
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
`;

const MainText = styled.h2`
  font: ${({ theme }) => theme.fonts.heading4};
  color: ${({ theme }) => theme.colors.textPrimary};
  margin: 0;
`;

const SubText = styled.p`
  font: ${({ theme }) => theme.fonts.heading5};
  color: ${({ theme }) => theme.colors.textPrimary};
  margin: 0;
`;

const RatingSection = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
`;

const StarIcon = styled.span`
  font-size: 16px;
  line-height: 1;
`;

const RatingText = styled.span`
  font: ${({ theme }) => theme.fonts.body3};
  color: ${({ theme }) => theme.colors.textSecondary};
  margin-left: 4px;
`;

const ButtonSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
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

  &:hover {
    background: ${({ theme }) => theme.colors.primaryDark};
  }

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
