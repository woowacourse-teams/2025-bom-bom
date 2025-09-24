import styled from '@emotion/styled';
import { StreakWrapper } from './ReadingStatusCard';
import ReadingStatusCardContainer from './ReadingStatusCardContainer';
import ProgressWithLabelSkeleton from '@/components/ProgressWithLabel/ProgressWithLabelSkeleton';
import TextSkeleton from '@/components/Skeleton/TextSkeleton';
import { useDevice } from '@/hooks/useDevice';
import { skeletonStyle } from '@/styles/skeleton';

const ReadingStatusCardSkeleton = () => {
  const device = useDevice();
  const isPC = device === 'pc';

  return (
    <ReadingStatusCardContainer>
      <StreakWrapper device={device}>
        <StreakIcon />
        <TextSkeleton width="60px" height="28px" />
      </StreakWrapper>

      <ProgressWithLabelSkeleton
        hasShowGraph={isPC}
        hasShowDescription={isPC}
      />

      <ProgressWithLabelSkeleton
        hasShowGraph={isPC}
        hasShowDescription={isPC}
      />
    </ReadingStatusCardContainer>
  );
};

export default ReadingStatusCardSkeleton;

const StreakIcon = styled.div`
  width: 70px;
  height: 70px;
  padding: 18px;
  border-radius: 36px;

  display: flex;
  align-items: center;
  justify-content: center;

  ${skeletonStyle}
`;
