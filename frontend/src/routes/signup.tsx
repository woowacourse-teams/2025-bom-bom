import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import { useDevice } from '@/hooks/useDevice';
import SignupCard from '@/pages/signup/components/SignupCard';
import { getResponsiveValue } from '@/utils/responsive';
import type { Device } from '@/hooks/useDevice';

export const Route = createFileRoute('/signup')({
  component: RouteComponent,
});

function RouteComponent() {
  const device = useDevice();

  return (
    <Container device={device}>
      <ContentWrapper device={device}>
        <SignupCard />
      </ContentWrapper>
    </Container>
  );
}

const Container = styled.main<{ device: Device }>`
  min-height: 100vh;
  padding: ${({ device }) => getResponsiveValue(device, 16, 24, 32)};

  display: flex;
  align-items: center;
  justify-content: center;

  background: ${({ device, theme }) =>
    device === 'mobile'
      ? theme.colors.white
      : `linear-gradient(135deg, ${theme.colors.primary} 0%, #f74 25%, ${theme.colors.primaryLight} 100%)`};
`;

const ContentWrapper = styled.div<{ device: Device }>`
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: ${({ device }) => getResponsiveValue(device, 400, 520, 600)};

  display: flex;
  gap: ${({ device }) => getResponsiveValue(device, 20, 28, 32)};
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;
