import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import LoginCard from '../pages/login/components/LoginCard';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';

export const Route = createFileRoute('/login')({
  component: Login,
});

function Login() {
  const deviceType = useDeviceType();

  return (
    <Container deviceType={deviceType}>
      <LoginCard deviceType={deviceType} />
      <InfoText deviceType={deviceType}>
        {deviceType === 'pc'
          ? '이미 계정이 있으신가요? Google로 로그인하면 자동으로 연결됩니다.'
          : '이미 계정이 있으신가요? \n Google로 로그인하면 자동으로 연결됩니다.'}
      </InfoText>
    </Container>
  );
}

const Container = styled.div<{ deviceType: DeviceType }>`
  min-height: 100vh;
  padding: 12px;

  display: flex;
  gap: 28px;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  background-color: ${({ deviceType, theme }) =>
    deviceType === 'pc' ? theme.colors.primary : theme.colors.white};
`;

const InfoText = styled.p<{ deviceType: DeviceType }>`
  color: ${({ deviceType, theme }) =>
    deviceType === 'pc' ? theme.colors.white : theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
  white-space: pre-line;
`;
