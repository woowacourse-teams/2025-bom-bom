import styled from '@emotion/styled';

export default function SignupCard() {
  return (
    <Container>
      <EmailInput type="email" placeholder="이메일" />
      <SubmitButton type="submit">회원가입</SubmitButton>
    </Container>
  );
}

const Container = styled.section`
  display: flex;
  gap: 22px;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  width: 410px;
  height: 540px;
  padding: 28px;
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgb(0 0 0 / 25%);

  background-color: ${({ theme }) => theme.colors.white};
`;

const EmailInput = styled.input`
  width: 100%;
  height: 48px;
  padding: 12px 16px;
  border: 1px solid ${({ theme }) => theme.colors.black};
  border-radius: 12px;
`;

const SubmitButton = styled.button`
  width: 100%;
  height: 48px;
  border-radius: 12px;

  background-color: ${({ theme }) => theme.colors.primary};

  color: ${({ theme }) => theme.colors.white};
`;
