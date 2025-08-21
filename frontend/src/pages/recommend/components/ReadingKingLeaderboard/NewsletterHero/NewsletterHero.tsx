import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import { Link } from '@tanstack/react-router';
import { queries } from '@/apis/queries';
import logo from '#/assets/logo.png';

export default function NewsletterHero() {
  const { data: userInfo } = useQuery(queries.me());

  return (
    <>
      {!userInfo && (
        <Container>
          <HeroContent>
            <HeroIcon>
              <img src={logo} alt="logo" width={48} height={48} />
            </HeroIcon>
            <HeroTitle>새로운 뉴스레터를 발견해보세요! 📚</HeroTitle>
            <HeroSubtitle>
              당신의 관심사에 맞는 최고의 뉴스레터를 추천해드립니다.
            </HeroSubtitle>
            <CTAButton to="/login">로그인하고 맞춤 추천 받기</CTAButton>
          </HeroContent>
        </Container>
      )}
    </>
  );
}

const Container = styled.div`
  overflow: hidden;
  width: 100%;
  margin: 0 auto 18px;
  border-radius: 16px;

  background: transparent;
`;

const HeroContent = styled.div`
  z-index: ${({ theme }) => theme.zIndex.content};
  width: 100%;
  padding: 56px;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  background: linear-gradient(135deg, #f96 0%, #ffb366 100%);
  text-align: center;
`;

const HeroIcon = styled.div`
  margin-bottom: 16px;
  font: ${({ theme }) => theme.fonts.heading1};
`;

const HeroTitle = styled.h1`
  margin-bottom: 12px;

  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.heading3};
`;

const HeroSubtitle = styled.p`
  margin-bottom: 24px;

  color: ${({ theme }) => theme.colors.white};
  font: ${({ theme }) => theme.fonts.body1};

  opacity: 0.9;
`;

const CTAButton = styled(Link)`
  width: fit-content;
  padding: 12px 24px;
  border: none;
  border-radius: 12px;

  background: ${({ theme }) => theme.colors.white};
  color: ${({ theme }) => theme.colors.primary};
  font: ${({ theme }) => theme.fonts.body2};

  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgb(0 0 0 / 15%);
    transform: translateY(-2px);
  }
`;
