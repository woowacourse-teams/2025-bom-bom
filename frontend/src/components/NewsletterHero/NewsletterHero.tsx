import styled from '@emotion/styled';

export default function NewsletterHero() {
  return (
    <HeroContainer>
      <HeroContent>
        <HeroIcon>🌸</HeroIcon>
        <HeroTitle>새로운 뉴스레터를 발견해보세요! 📚</HeroTitle>
        <HeroSubtitle>
          당신의 관심사에 맞는 최고의 뉴스레터를 추천해드립니다.
        </HeroSubtitle>
        <CTAButton>로그인하고 맞춤 추천 받기</CTAButton>
      </HeroContent>
    </HeroContainer>
  );
}

const HeroContainer = styled.div`
  background: linear-gradient(135deg, #ff9966 0%, #ffb366 100%);
  border-radius: 16px;
  padding: 40px;
  margin-bottom: 32px;
  position: relative;
  overflow: hidden;
`;

const HeroContent = styled.div`
  text-align: center;
  position: relative;
  z-index: 2;
`;

const HeroIcon = styled.div`
  font-size: 48px;
  margin-bottom: 16px;
`;

const HeroTitle = styled.h1`
  ${({ theme }) => theme.fonts.heading3};
  color: ${({ theme }) => theme.colors.white};
  margin-bottom: 12px;
`;

const HeroSubtitle = styled.p`
  ${({ theme }) => theme.fonts.body1};
  color: ${({ theme }) => theme.colors.white};
  opacity: 0.9;
  margin-bottom: 24px;
`;

const CTAButton = styled.button`
  ${({ theme }) => theme.fonts.body2};
  background: ${({ theme }) => theme.colors.white};
  color: ${({ theme }) => theme.colors.primary};
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
`;
