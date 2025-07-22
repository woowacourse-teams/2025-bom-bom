import styled from '@emotion/styled';
import SparklesIcon from '../../../components/icons/SparklesIcon';
import GoogleIcon from '../../../components/icons/GoogleIcon';

function LoginCard() {
  return (
    <Container>
      <GreetingWrapper>
        <IconWrapper>
          <SparklesIcon />
        </IconWrapper>
        <GreetingTitle>봄봄에 오신 걸 환영해요</GreetingTitle>
        <GreetingMessage>
          당신의 하루에 찾아오는 작은 설렘{'\n'}뉴스레터를 한 곳에서 쉽게
          관리하세요
        </GreetingMessage>
      </GreetingWrapper>
      <Divider />
      <GoogleButton type="button">
        <GoogleIcon />
        Google로 시작하기
      </GoogleButton>
      <Terms>
        로그인하시면 봄봄의 <Highlight>서비스 약관</Highlight>과{' '}
        <Highlight>개인정보 처리방침</Highlight>에{'\n'}
        동의하는 것으로 간주됩니다.
      </Terms>
    </Container>
  );
}

export default LoginCard;

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

const GreetingWrapper = styled.div`
  display: flex;
  gap: 22px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const IconWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;

  padding: 18px;
  border-radius: 50%;
  box-shadow:
    0 20px 25px -5px ${({ theme }) => `${theme.colors.primaryLight}40`},
    0 10px 10px -5px ${({ theme }) => `${theme.colors.primaryLight}20`};

  background-color: ${({ theme }) => theme.colors.primary};
`;

const GreetingTitle = styled.h2`
  background: linear-gradient(107deg, #181818 0%, #f96 100%);
  background-clip: text;

  font: ${({ theme }) => theme.fonts.heading3};
  text-align: center;

  -webkit-text-fill-color: transparent;
`;

const GreetingMessage = styled.p`
  margin: 34px 0;

  color: ${({ theme }) => theme.colors.textSecondary};
  font: ${({ theme }) => theme.fonts.heading5};
  font-weight: 400;
  text-align: center;
  white-space: pre-line;
`;

const Divider = styled.div`
  width: 100%;
  height: 2px;
  margin-bottom: 34px;

  background: linear-gradient(
    90deg,
    rgb(237 237 237 / 0%) 0%,
    ${({ theme }) => theme.colors.dividers} 50%,
    rgb(237 237 237 / 0%) 100%
  );
`;

const GoogleButton = styled.button`
  display: flex;
  gap: 18px;
  align-items: center;
  justify-content: center;

  width: 100%;
  padding: 12px;
  border: 2px solid ${({ theme }) => theme.colors.dividers};
  border-radius: 8px;
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 5%);

  background-color: ${({ theme }) => theme.colors.white};

  font: ${({ theme }) => theme.fonts.body1};
  text-align: center;

  &:hover {
    background-color: ${({ theme }) => theme.colors.primaryLight};
    transition: background-color 0.5s ease;
  }
`;

const Terms = styled.p`
  color: ${({ theme }) => theme.colors.textTertiary};
  font: ${({ theme }) => theme.fonts.caption};
  text-align: center;
  white-space: pre-line;
`;

const Highlight = styled.span`
  color: ${({ theme }) => theme.colors.primary};
`;
