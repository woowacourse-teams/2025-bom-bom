import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import LoginCard from '../pages/login/components/LoginCard';

export const Route = createFileRoute('/login')({
  component: Login,
});

function Login() {
  return (
    <Container>
      <LoginCard />
      <InfoText>
        이미 계정이 있으신가요? Google로 로그인하면 자동으로 연결됩니다.
      </InfoText>
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

const InfoText = styled.p`
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.body2};

  opacity: 0.8;
`;
