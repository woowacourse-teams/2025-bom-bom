import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import LoginCard from '../pages/login/components/LoginCard';
import { useDeviceType, DeviceType } from '@/hooks/useDeviceType';
import { getResponsiveValue } from '@/utils/responsive';

export const Route = createFileRoute('/login')({
  component: Login,
});

function Login() {
  const deviceType = useDeviceType();

  return (
    <Container deviceType={deviceType}>
      <ContentWrapper deviceType={deviceType}>
        <LoginCard />
        <InfoText deviceType={deviceType}>
          {deviceType === 'mobile'
            ? '이미 계정이 있으신가요? \n Google로 로그인하면 자동으로 연결됩니다.'
            : '이미 계정이 있으신가요? Google로 로그인하면 자동으로 연결됩니다.'}
        </InfoText>
      </ContentWrapper>
    </Container>
  );
}

const Container = styled.main<{ deviceType: DeviceType }>`
  min-height: 100vh;
  padding: ${({ deviceType }) => getResponsiveValue(deviceType, 16, 24, 32)};

  display: flex;
  align-items: center;
  justify-content: center;

  background: ${({ deviceType, theme }) =>
    deviceType === 'mobile'
      ? theme.colors.white
      : `linear-gradient(135deg, ${theme.colors.primary} 0%, #f74 25%, ${theme.colors.primaryLight} 100%)`};
`;

const ContentWrapper = styled.div<{ deviceType: DeviceType }>`
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: ${({ deviceType }) =>
    getResponsiveValue(deviceType, 400, 520, 600)};

  display: flex;
  gap: ${({ deviceType }) => getResponsiveValue(deviceType, 20, 28, 32)};
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const InfoText = styled.p<{ deviceType: DeviceType }>`
  max-width: 320px;
  margin: 0;

  color: ${({ deviceType, theme }) =>
    deviceType === 'mobile' ? theme.colors.textSecondary : theme.colors.white};
  font: ${({ theme }) => theme.fonts.body2};
  text-align: center;
  white-space: pre-line;
`;
