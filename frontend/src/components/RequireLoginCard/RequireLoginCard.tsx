import styled from '@emotion/styled';
import { useNavigate } from '@tanstack/react-router';
import Button from '../Button/Button';
import { useDevice } from '@/hooks/useDevice';
import LockIcon from '#/assets/svg/lock.svg';

const RequireLoginCard = () => {
  const navigate = useNavigate();
  const device = useDevice();
  const isMobile = device === 'mobile';

  return (
    <Container isMobile={isMobile}>
      <StyledLockIcon />
      <Title>로그인이 필요해요</Title>
      <DescriptionWrapper>
        <Lead isMobile={isMobile}>
          현재 페이지를 이용하시려면 먼저 로그인해 주세요
        </Lead>
        <Support isMobile={isMobile}>
          봄봄에서 더 많은 특별한 기능들을 만나보실 수 있어요!
        </Support>
      </DescriptionWrapper>
      <GoToLoginButton
        text="봄봄 시작하기"
        onClick={() => {
          navigate({ to: '/login' });
        }}
      />
    </Container>
  );
};

export default RequireLoginCard;

const Container = styled.section<{ isMobile: boolean }>`
  width: ${({ isMobile }) => (isMobile ? '100%' : '380px')};
  height: 500px;
  margin: auto 0;
  padding: ${({ isMobile }) => (isMobile ? '16px' : '28px')};

  display: flex;
  gap: 24px;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.white};

  ${({ isMobile }) =>
    !isMobile &&
    `
    border-radius: 20px;
    box-shadow: 0 25px 50px -12px rgb(0 0 0 / 25%);
  
  `}
`;

const StyledLockIcon = styled(LockIcon)`
  width: 52px;
  height: 52px;

  color: ${({ theme }) => theme.colors.primary};
`;

const Title = styled.h1`
  font: ${({ theme }) => theme.fonts.heading3};
`;

const DescriptionWrapper = styled.div`
  display: flex;
  gap: 4px;
  flex-direction: column;
`;

const Lead = styled.p<{ isMobile: boolean }>`
  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ isMobile, theme }) =>
    isMobile ? theme.fonts.body2 : theme.fonts.body1};
  text-align: center;
`;

const Support = styled.p<{ isMobile: boolean }>`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ isMobile, theme }) =>
    isMobile ? theme.fonts.body3 : theme.fonts.body2};
  text-align: center;
`;

const GoToLoginButton = styled(Button)`
  width: 100%;
  max-width: 380px;

  font: ${({ theme }) => theme.fonts.heading5};
`;
