import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import ReadingKingLeaderboard from '../../pages/recommend/components/ReadingKingLeaderboard/ReadingKingLeaderboard';
import TrendySection from '../../pages/recommend/components/ReadingKingLeaderboard/TrendySection/TrendySection';

export const Route = createFileRoute('/_bombom/recommend')({
  component: Recommend,
});

function Recommend() {
  return (
    <Container>
      <MainSection>
        <TrendySection />
      </MainSection>
      <SideSection>
        <ReadingKingLeaderboard />
      </SideSection>
    </Container>
  );
}

const Container = styled.div`
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px 20px;

  display: flex;
  gap: 32px;
  align-items: flex-start;

  @media (width <= 1024px) {
    gap: 24px;
    padding: 20px 16px;
  }

  @media (width <= 768px) {
    flex-direction: column;
    gap: 20px;
    padding: 16px;
  }
`;

const MainSection = styled.section`
  flex: 1;
  max-width: 840px;
  min-width: 0;
  
  display: flex;
  gap: 24px;
  flex-direction: column;

  @media (width <= 768px) {
    max-width: none;
    width: 100%;
  }
`;

const SideSection = styled.div`
  width: 400px;
  flex-shrink: 0;

  @media (width <= 1024px) {
    width: 360px;
  }

  @media (width <= 768px) {
    width: 100%;
    max-width: 400px;
    margin: 0 auto;
  }
`;
