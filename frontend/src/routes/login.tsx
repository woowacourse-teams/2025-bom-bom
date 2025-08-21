import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import LoginCard from '../pages/login/components/LoginCard';
import { useDeviceType } from '@/hooks/useDeviceType';

export const Route = createFileRoute('/login')({
  component: Login,
});

function Login() {
  const deviceType = useDeviceType();
  const isMobile = deviceType === 'mobile';

  return (
    <Container isMobile={isMobile}>
      <LoginCard isMobile={isMobile} />
      <InfoText isMobile={isMobile}>
        {isMobile
          ? '이미 계정이 있으신가요? \n Google로 로그인하면 자동으로 연결됩니다.'
          : '이미 계정이 있으신가요? Google로 로그인하면 자동으로 연결됩니다.'}
      </InfoText>
    </Container>
  );
}

const Container = styled.div<{ isMobile: boolean }>`
  overflow: hidden;
  height: 100dvh;
  padding: 12px;

  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '16px' : '28px')};
  flex-direction: column;
  align-items: center;
  justify-content: center;

  background-color: ${({ isMobile, theme }) =>
    isMobile ? theme.colors.white : theme.colors.primary};
`;

const InfoText = styled.p<{ isMobile: boolean }>`
  color: ${({ isMobile, theme }) =>
    isMobile ? theme.colors.textSecondary : theme.colors.white};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
  white-space: pre-line;
`;
