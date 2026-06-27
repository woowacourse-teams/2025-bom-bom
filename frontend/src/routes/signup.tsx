import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import SignupCard from '@/pages/signup/components/SignupCard';

export const Route = createFileRoute('/signup')({
  component: RouteComponent,
});

function RouteComponent() {
  return (
    <Container>
      <SignupCard />
    </Container>
  );
}

const Container = styled.div`
  min-height: 100vh;

  display: flex;
  gap: 28px;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;
