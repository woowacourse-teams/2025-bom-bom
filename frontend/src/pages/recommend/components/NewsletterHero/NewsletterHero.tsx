import styled from '@emotion/styled';
import { Link } from '@tanstack/react-router';
import SlideCardList from '../SlideCardList/SlideCardList';
import { useDevice } from '@/hooks/useDevice';
import { useUserInfo } from '@/hooks/useUserInfo';
import { skeletonStyle } from '@/styles/skeleton';
import logo from '#/assets/logo.png';

const NewsletterHero = () => {
  const { userInfo, isLoading } = useUserInfo();
  const device = useDevice();
  const isPC = device === 'pc';

  if (isLoading) {
    return (
      <Container>
        <SkeletonBanner />
      </Container>
    );
  }

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
            <CTAButton to="/login" isPC={isPC}>
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

const CTAButton = styled(Link)<{ isPC: boolean }>`
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

const SkeletonBanner = styled.div`
  height: 280px;

  ${skeletonStyle}
`;
