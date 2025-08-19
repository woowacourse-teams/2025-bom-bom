import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import { useDeviceType } from '@/hooks/useDeviceType';
import SignupCard from '@/pages/signup/components/SignupCard';

export const Route = createFileRoute('/signup')({
  component: RouteComponent,
});

function RouteComponent() {
  const deviceType = useDeviceType();
  const isMobile = deviceType === 'mobile';

  return (
    <Container isMobile={isMobile}>
      <SignupCard isMobile={isMobile} />
    </Container>
  );
}

const Container = styled.div<{ isMobile: boolean }>`
  min-height: 100vh;

  display: flex;
  gap: 28px;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  background-color: ${({ isMobile, theme }) =>
    isMobile ? theme.colors.white : theme.colors.primary};
`;
