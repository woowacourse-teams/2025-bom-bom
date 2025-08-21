import styled from '@emotion/styled';
import { createFileRoute } from '@tanstack/react-router';
import ReadingKingLeaderboard from '../../pages/recommend/components/ReadingKingLeaderboard/ReadingKingLeaderboard';
import TrendySection from '../../pages/recommend/components/ReadingKingLeaderboard/TrendySection/TrendySection';
import { useDeviceType, type DeviceType } from '@/hooks/useDeviceType';
import NewsletterHero from '@/pages/recommend/components/ReadingKingLeaderboard/NewsletterHero/NewsletterHero';

export const Route = createFileRoute('/_bombom/recommend')({
  component: Recommend,
});

function Recommend() {
  const device = useDeviceType();

  return (
    <Container device={device}>
      <MainSection device={device}>
        <NewsletterHero />
        <TrendySection />
      </MainSection>
      <SideSection device={device}>
        <ReadingKingLeaderboard />
      </SideSection>
    </Container>
  );
}

const Container = styled.div<{ device: DeviceType }>`
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;

  display: flex;
  gap: ${({ device }) =>
    device === 'mobile' ? '20px' : device === 'tablet' ? '24px' : '32px'};
  flex-direction: ${({ device }) => (device === 'mobile' ? 'column' : 'row')};
  align-items: flex-start;
`;

const MainSection = styled.section<{ device: DeviceType }>`
  width: ${({ device }) => (device === 'mobile' ? '100%' : 'auto')};
  min-width: 0;
  max-width: ${({ device }) => (device === 'mobile' ? 'none' : '840px')};

  display: flex;
  gap: 24px;
  flex: 1;
  flex-direction: column;
`;

const SideSection = styled.div<{ device: DeviceType }>`
  width: ${({ device }) =>
    device === 'mobile' ? '100%' : device === 'tablet' ? '360px' : '400px'};
  max-width: ${({ device }) => (device === 'mobile' ? '400px' : 'none')};
  margin: ${({ device }) => (device === 'mobile' ? '0 auto' : '0')};

  flex-shrink: 0;
`;
