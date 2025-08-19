import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import LoginCard from '../pages/login/components/LoginCard';
import { useDeviceType } from '@/hooks/useDeviceType';

export const Route = createFileRoute('/login')({
  component: Login,
});

function Login() {
  const deviceType = useDeviceType();
  const isPC = deviceType === 'pc';

  return (
    <Container isPC={isPC}>
      <LoginCard isPC={isPC} />
      <InfoText isPC={isPC}>
        {isPC
          ? '이미 계정이 있으신가요? Google로 로그인하면 자동으로 연결됩니다.'
          : '이미 계정이 있으신가요? \n Google로 로그인하면 자동으로 연결됩니다.'}
      </InfoText>
    </Container>
  );
}

const Container = styled.div<{ isPC: boolean }>`
  min-height: 100vh;
  padding: 12px;

  display: flex;
  gap: ${({ isPC }) => (isPC ? '28px' : '16px')};
  flex-direction: column;
  align-items: center;
  justify-content: center;

  background-color: ${({ isPC, theme }) =>
    isPC ? theme.colors.primary : theme.colors.white};
`;

const InfoText = styled.p<{ isPC: boolean }>`
  color: ${({ isPC, theme }) =>
    isPC ? theme.colors.white : theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
  white-space: pre-line;
`;
