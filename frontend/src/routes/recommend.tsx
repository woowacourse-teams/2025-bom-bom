import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import PageLayout from '../components/PageLayout/PageLayout';
import ReadingKingLeaderboard from '../pages/recommend/components/ReadingKingLeaderboard/ReadingKingLeaderboard';
import NewsletterHero from '../pages/recommend/components/ReadingKingLeaderboard/NewsletterHero/NewsletterHero';
import ImageInfoCard from '../components/ImageInfoCard/ImageInfoCard';
import Chip from '../components/Chip/Chip';
import { TRENDY_NEWSLETTERS } from '../mocks/trendyNewsLetter';
import { CATEGORIES } from '../constants/category';
import trendingUpIcon from '../../public/assets/trending-up.svg';

export const Route = createFileRoute('/recommend')({
  component: Recommend,
});

function Recommend() {
  return (
    <PageLayout activeNav="recommend">
      <Container>
        <MainSection>
          <NewsletterHero />
          <TrendySection>
            <SectionHeader>
              <SectionIcon>
                <img src={trendingUpIcon} alt="trending up" />
              </SectionIcon>
              <SectionTitle>트렌디한 뉴스레터</SectionTitle>
            </SectionHeader>
            <TagContainer>
              {CATEGORIES.map((category, index) => (
                <Chip key={index} text={category} selected={index === 0} />
              ))}
            </TagContainer>
            <TrendyGrid>
              {TRENDY_NEWSLETTERS.map((newsletter, index) => (
                <ImageInfoCard
                  key={index}
                  imageUrl={newsletter.imageUrl}
                  title={newsletter.title}
                  description={newsletter.description}
                />
              ))}
            </TrendyGrid>
          </TrendySection>
        </MainSection>
        <SideSection>
          <ReadingKingLeaderboard />
        </SideSection>
      </Container>
    </PageLayout>
  );
}

const Container = styled.div`
  display: flex;
  align-items: flex-start;
  justify-content: center;

  width: 100%;
  max-width: 1280px;
  padding: 64px 20px 0;

  gap: 24px;

  @media (width <= 768px) {
    flex-direction: column;
    align-items: center;
  }
`;

const MainSection = styled.section`
  display: flex;
  flex: 1;
  flex-direction: column;

  max-width: 840px;

  gap: 24px;
`;

const SideSection = styled.div`
  flex-shrink: 0;
  width: 400px;

  @media (width <= 768px) {
    width: 100%;
    max-width: 400px;
  }
`;

const TrendySection = styled.div`
  width: 100%;
  padding: 24px;
  border: 1px solid ${({ theme }) => theme.colors.stroke};
  border-radius: 20px;
  box-shadow:
    0 10px 15px -3px rgb(0 0 0 / 10%),
    0 4px 6px -4px rgb(0 0 0 / 10%);

  background: ${({ theme }) => theme.colors.white};

  backdrop-filter: blur(10px);
`;

const SectionHeader = styled.div`
  display: flex;
  align-items: center;

  margin-bottom: 16px;

  gap: 8px;
`;

const SectionIcon = styled.span`
  display: flex;
  align-items: center;
  justify-content: center;

  width: 28px;
  height: 28px;
  border-radius: 12px;

  background: ${({ theme }) => theme.colors.primary};

  font: ${({ theme }) => theme.fonts.body1};
`;

const SectionTitle = styled.h2`
  margin: 0;

  color: ${({ theme }) => theme.colors.black};
  font: ${({ theme }) => theme.fonts.heading5};
`;

const TagContainer = styled.div`
  display: flex;
  flex-wrap: wrap;

  margin-bottom: 16px;

  gap: 8px;
`;

const TrendyGrid = styled.div`
  display: grid;

  gap: 8px;
  grid-template-columns: repeat(2, 1fr);

  @media (width <= 768px) {
    grid-template-columns: 1fr;
  }
`;
