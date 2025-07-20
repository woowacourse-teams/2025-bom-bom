import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import PageLayout from '../components/PageLayout/PageLayout';
import ReadingKingLeaderboard from '../pages/recommend/components/ReadingKingLeaderboard/ReadingKingLeaderboard';
import NewsletterHero from '../pages/recommend/components/ReadingKingLeaderboard/NewsletterHero/NewsletterHero';
import ImageInfoCard from '../components/ImageInfoCard/ImageInfoCard';
import { TRENDY_NEWSLETTERS } from '../mocks/trendyNewsLetter';

export const Route = createFileRoute('/recommend')({
  component: Index,
});

function Index() {
  return (
    <PageLayout activeNav="recommend">
      <Container>
        <MainSection>
          <NewsletterHero />
          <TrendySection>
            <SectionHeader>
              <SectionIcon>📊</SectionIcon>
              <SectionTitle>트렌디한 뉴스레터</SectionTitle>
            </SectionHeader>
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
  margin: 0 auto;
  padding: 64px 20px 0;

  gap: 24px;

  @media (width <= 768px) {
    flex-direction: column;
    align-items: center;
  }
`;

const MainSection = styled.div`
  display: flex;
  flex: 1;
  flex-direction: column;

  width: 100%;
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
`;

const SectionHeader = styled.div`
  display: flex;
  align-items: center;

  margin-bottom: 16px;

  gap: 8px;
`;

const SectionIcon = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;

  width: 28px;
  height: 28px;
  border-radius: 12px;

  background: #f96;

  font-size: 14px;
`;

const SectionTitle = styled.h2`
  margin: 0;

  color: #0f172b;
  font-family: Inter, 'Noto Sans KR', sans-serif;
  font-weight: 400;
  font-size: 17.5px;
  line-height: 24.5px;
`;

const TrendyGrid = styled.div`
  display: grid;

  gap: 8px;
  grid-template-columns: repeat(2, 1fr);

  @media (width <= 768px) {
    grid-template-columns: 1fr;
  }
`;
