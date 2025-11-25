import styled from '@emotion/styled';
import Button from '@/components/Button/Button';
import { useDevice } from '@/hooks/useDevice';

interface NewsletterUnsubscribeModalProps {
  onClose: () => void;
  onUnsubscribe: () => void;
}

const NewsletterUnsubscribeConfirmation = ({
  onClose,
  onUnsubscribe,
}: NewsletterUnsubscribeModalProps) => {
  const device = useDevice();
  const isMobile = device === 'mobile';

  return (
    <Container isMobile={isMobile}>
      <Title isMobile={isMobile}>구독을 해지하러 이동하시겠습니까?</Title>

      <UnsubscribeDescription isMobile={isMobile}>
        구독을 해지하면 해당 뉴스레터의{'\n'}새로운 소식을 받아볼 수 없습니다.
      </UnsubscribeDescription>

      <ModalButtonWrapper>
        <ModalButton
          isMobile={isMobile}
          variant="outlined"
          text="네, 해지할래요"
          onClick={() => {
            onUnsubscribe();
            onClose();
          }}
        />
        <ModalButton
          isMobile={isMobile}
          text="아니요, 유지할래요"
          onClick={onClose}
        />
      </ModalButtonWrapper>
    </Container>
  );
};

export default NewsletterUnsubscribeConfirmation;

const Container = styled.div<{ isMobile: boolean }>`
  width: 100%;
  min-width: ${({ isMobile }) => (isMobile ? '280px' : '440px')};

  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '12px' : '20px')};
  flex-direction: column;
  align-items: center;
  justify-content: center;

  text-align: center;
`;

const Title = styled.h2<{ isMobile: boolean }>`
  color: ${({ theme }) => theme.colors.textPrimary};
  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.heading5 : theme.fonts.heading4};
`;

const UnsubscribeDescription = styled.p<{ isMobile: boolean }>`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.body3 : theme.fonts.body2};
  white-space: pre-wrap;
`;

const ModalButtonWrapper = styled.div`
  width: 100%;

  display: flex;
  gap: 12px;
  justify-content: center;
`;

const ModalButton = styled(Button)<{ isMobile: boolean }>`
  height: 48px;
  min-width: ${({ isMobile }) => (isMobile ? '120px' : '160px')};
  border-radius: 8px;

  font: ${({ theme, isMobile }) =>
    isMobile ? theme.fonts.body3 : theme.fonts.body2};

  word-break: keep-all;
`;
