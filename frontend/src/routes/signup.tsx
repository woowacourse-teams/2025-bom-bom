import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import { useDeviceType, DeviceType } from '@/hooks/useDeviceType';
import SignupCard from '@/pages/signup/components/SignupCard';
import { getResponsiveValue } from '@/utils/responsive';

export const Route = createFileRoute('/signup')({
  component: RouteComponent,
});

function RouteComponent() {
  const deviceType = useDeviceType();

  return (
    <Container deviceType={deviceType}>
      <ContentWrapper deviceType={deviceType}>
        <SignupCard />
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
