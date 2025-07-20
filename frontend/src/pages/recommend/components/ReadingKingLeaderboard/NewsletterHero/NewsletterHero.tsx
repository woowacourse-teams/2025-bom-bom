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
  overflow: hidden;
  position: relative;

  margin-bottom: 32px;
  padding: 40px;
  border-radius: 16px;

  background: linear-gradient(${({ theme }) => theme.gradients.primary});
`;

const HeroContent = styled.div`
  position: relative;
  z-index: 2;

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

const CTAButton = styled.button`
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
