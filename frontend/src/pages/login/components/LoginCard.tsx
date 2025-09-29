import styled from '@emotion/styled';
import { ENV } from '@/apis/env';
import { useDevice } from '@/hooks/useDevice';
import { isWeb, isIOS } from '@/libs/webview/webview.utils';
import { theme } from '@/styles/theme';
import { isLocal } from '@/utils/environment';
import AppleIcon from '#/assets/svg/apple.svg';
import GoogleIcon from '#/assets/svg/google.svg';
import SparklesIcon from '#/assets/svg/sparkles.svg';

const LoginCard = () => {
  const device = useDevice();
  const isMobile = device === 'mobile';

  return (
    <Container isMobile={isMobile}>
      <GreetingWrapper isMobile={isMobile}>
        <IconWrapper>
          <SparklesIcon
            width={24}
            height={24}
            fill={theme.colors.white}
            color={theme.colors.white}
          />
        </IconWrapper>
        <GreetingTitle>봄봄에 오신 걸 환영해요</GreetingTitle>
        <GreetingMessage isMobile={isMobile}>
          당신의 하루에 찾아오는 작은 설렘{'\n'}뉴스레터를 한 곳에서 쉽게
          관리하세요
        </GreetingMessage>
      </GreetingWrapper>
      <Divider />
      <LoginButton
        onClick={() => {
          const envQuery = isLocal ? '?env=local' : '';
          window.location.href = `${ENV.baseUrl}/auth/login/google${envQuery}`;
        }}
        type="button"
      >
        <GoogleIcon width={24} height={24} fill="black" />
        Google로 계속하기
      </LoginButton>
      {(isWeb() || isIOS()) && (
        <LoginButton
          onClick={() => {
            const envQuery = isLocal ? '?env=local' : '';
            window.location.href = `${ENV.baseUrl}/auth/login/apple${envQuery}`;
          }}
          type="button"
        >
          <AppleIcon width={24} height={24} fill="black" />
          Apple로 계속하기
        </LoginButton>
      )}
      <Terms>
        로그인하시면 봄봄의 <Highlight>서비스 약관</Highlight>과
        <Highlight>개인정보 처리방침</Highlight>에{'\n'}
        동의하는 것으로 간주됩니다.
      </Terms>
    </Container>
  );
};

export default LoginCard;

const Container = styled.section<{ isMobile: boolean }>`
  width: min(100%, 420px);
  padding: 28px;

  display: flex;
  gap: 16px;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  ${({ isMobile }) =>
    !isMobile &&
    `
    border-radius: 20px;
    box-shadow: 0 25px 50px -12px rgb(0 0 0 / 25%);
    background-color: ${theme.colors.white};
  
  `}
`;

const GreetingWrapper = styled.div<{ isMobile: boolean }>`
  display: flex;
  gap: ${({ isMobile }) => (isMobile ? '16px' : '20px')};
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const IconWrapper = styled.div`
  padding: 18px;
  border-radius: 50%;
  box-shadow:
    0 20px 25px -5px ${({ theme }) => `${theme.colors.primaryLight}40`},
    0 10px 10px -5px ${({ theme }) => `${theme.colors.primaryLight}20`};

  display: flex;
  align-items: center;
  justify-content: center;

  background-color: ${({ theme }) => theme.colors.primary};
`;

const GreetingTitle = styled.h2`
  background: linear-gradient(107deg, #181818 0%, #f96 100%);
  background-clip: text;
  font: ${({ theme }) => theme.fonts.heading3};
  text-align: center;

  -webkit-text-fill-color: transparent;
`;

const GreetingMessage = styled.p<{ isMobile: boolean }>`
  margin: ${({ isMobile }) => (isMobile ? '24px' : '34px')};

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

const LoginButton = styled.button`
  width: 100%;
  padding: 12px;
  border: 2px solid ${({ theme }) => theme.colors.dividers};
  border-radius: 8px;
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 5%);

  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;

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
