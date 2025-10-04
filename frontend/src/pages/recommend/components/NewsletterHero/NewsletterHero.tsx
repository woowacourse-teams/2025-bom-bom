import styled from '@emotion/styled';
import { useNavigate } from '@tanstack/react-router';
import SlideCardList from '../SlideCardList/SlideCardList';
import Skeleton from '@/components/Skeleton/Skeleton';
import { useDevice } from '@/hooks/useDevice';
import { useUserInfo } from '@/hooks/useUserInfo';
import { trackEvent } from '@/libs/googleAnalytics/gaEvents';
import { isIOS, sendMessageToRN } from '@/libs/webview/webview.utils';
import logo from '#/assets/avif/logo.avif';

const NewsletterHero = () => {
  const { userInfo, isLoading } = useUserInfo();
  const navigate = useNavigate();
  const device = useDevice();
  const isPC = device === 'pc';

  if (isLoading) {
    return (
      <Container>
        <Skeleton width="100%" height="280px" />
      </Container>
    );
  }

  const handleLoginClick = () => {
    if (isIOS())
      sendMessageToRN({
        type: 'SHOW_LOGIN_SCREEN',
      });
    else navigate({ to: '/login' });
  };

  return (
    <>
      {userInfo ? (
        <SlideCardList />
      ) : (
        <Container>
          <HeroContent isPC={isPC}>
            <HeroIcon src={logo} alt="logo" width={48} height={48} />
            <HeroTitle isPC={isPC}>
              새로운 뉴스레터를 발견해보세요! 📚
            </HeroTitle>
            <HeroSubtitle isPC={isPC}>
              당신의 관심사에 맞는 최고의 뉴스레터를 추천해드립니다.
            </HeroSubtitle>
            <CTAButton
              isPC={isPC}
              onClick={() => {
                handleLoginClick();
                trackEvent({
                  category: 'Navigation',
                  action: '로그인 버튼 클릭',
                  label: '추천 페이지 Hero',
                });
              }}
            >
              로그인하고 맞춤 추천 받기
            </CTAButton>
          </HeroContent>
        </Container>
      )}
    </>
  );
};

export default NewsletterHero;

const Container = styled.div`
  overflow: hidden;
  width: 100%;
  border-radius: 16px;

  background: transparent;
`;

const HeroContent = styled.div<{ isPC: boolean }>`
  z-index: ${({ theme }) => theme.zIndex.content};
  width: 100%;
  height: 280px;
  padding: 56px;

  display: flex;
  gap: ${({ isPC }) => (isPC ? '16px' : '12px')};
  flex-direction: column;
  align-items: center;
  justify-content: center;

  background: linear-gradient(135deg, #f96 0%, #fe5e04 100%);
  text-align: center;
`;

const HeroIcon = styled.img``;

const HeroTitle = styled.h1<{ isPC: boolean }>`
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme, isPC }) =>
    isPC ? theme.fonts.heading3 : theme.fonts.heading4};

  word-break: keep-all;
`;

const HeroSubtitle = styled.p<{ isPC: boolean }>`
  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme, isPC }) => (isPC ? theme.fonts.body1 : theme.fonts.body2)};

  opacity: 0.9;
  word-break: keep-all;
`;

const CTAButton = styled.button<{ isPC: boolean }>`
  width: fit-content;
  padding: 12px 24px;
  border: none;
  border-radius: 12px;

  background: ${({ theme }) => theme.colors.white};
  color: ${({ theme }) => theme.colors.primary};
  font: ${({ theme, isPC }) => (isPC ? theme.fonts.body2 : theme.fonts.body3)};

  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgb(0 0 0 / 15%);
    transform: translateY(-2px);
  }
`;
