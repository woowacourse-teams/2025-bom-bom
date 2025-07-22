import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import PageLayout from '../components/PageLayout/PageLayout';
import NewsletterHero from '../pages/recommend/components/ReadingKingLeaderboard/NewsletterHero/NewsletterHero';
import ReadingKingLeaderboard from '../pages/recommend/components/ReadingKingLeaderboard/ReadingKingLeaderboard';
import TrendySection from '../pages/recommend/components/ReadingKingLeaderboard/TrendySection/TrendySection';
import { useQuery } from '@tanstack/react-query';
import { getNewsletters } from '../apis/newsLetters';

export const Route = createFileRoute('/recommend')({
  component: Recommend,
});

function Recommend() {
  const { data: newsletters } = useQuery({
    queryKey: ['newsletters'],
    queryFn: () => getNewsletters(),
  });

  if (!newsletters) return null;
  return (
    <PageLayout activeNav="recommend">
      <Container>
        <MainSection>
          <NewsletterHero />
          <TrendySection newsletters={newsletters} />
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
  gap: 24px;
  align-items: flex-start;
  justify-content: center;

  width: 100%;
  max-width: 1280px;
  padding: 64px 20px 0;

  @media (width <= 768px) {
    flex-direction: column;
    align-items: center;
  }
`;

const MainSection = styled.section`
  display: flex;
  gap: 24px;
  flex: 1;
  flex-direction: column;

  max-width: 840px;
`;

const SideSection = styled.div`
  flex-shrink: 0;
  width: 400px;

  @media (width <= 768px) {
    width: 100%;
    max-width: 400px;
  }
`;
