import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import NewsletterHero from '../../pages/recommend/components/ReadingKingLeaderboard/NewsletterHero/NewsletterHero';
import ReadingKingLeaderboard from '../../pages/recommend/components/ReadingKingLeaderboard/ReadingKingLeaderboard';
import TrendySection from '../../pages/recommend/components/ReadingKingLeaderboard/TrendySection/TrendySection';
import { DeviceType, useDeviceType } from '@/hooks/useDeviceType';

export const Route = createFileRoute('/_bombom/recommend')({
  component: Recommend,
});

function Recommend() {
  const deviceType = useDeviceType();

  return (
    <Container deviceType={deviceType}>
      <MainSection>
        <NewsletterHero />
        <TrendySection />
      </MainSection>
      <SideSection>
        <ReadingKingLeaderboard />
      </SideSection>
    </Container>
  );
}

const Container = styled.div<{ deviceType: DeviceType }>`
  width: 100%;
  max-width: 1280px;
  padding: ${({ deviceType }) => (deviceType === 'mobile' ? '0 16px' : '24px')};

  display: flex;
  gap: 24px;
  align-items: flex-start;
  justify-content: center;

  @media (width <= 768px) {
    flex-direction: column;
    align-items: center;
  }
`;

const MainSection = styled.section`
  max-width: 840px;

  display: flex;
  gap: 24px;
  flex: 1;
  flex-direction: column;
`;

const SideSection = styled.div`
  width: 400px;
  flex-shrink: 0;

  @media (width <= 768px) {
    width: 100%;
    max-width: 400px;
  }
`;
