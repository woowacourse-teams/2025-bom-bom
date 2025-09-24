import styled from '@emotion/styled';
import { StreakWrapper, WeeklyProgressContainer } from './ReadingStatusCard';
import ReadingStatusCardContainer from './ReadingStatusCardContainer';
import TextSkeleton from '@/components/Skeleton/TextSkeleton';
import { useDevice } from '@/hooks/useDevice';
import { skeletonStyle } from '@/styles/skeleton';

const ReadingStatusCardSkeleton = () => {
  const device = useDevice();

  return (
    <ReadingStatusCardContainer>
      <StreakWrapper device={device}>
        <StreakIcon />
        <TextSkeleton width="60px" height="28px" />
      </StreakWrapper>

      <WeeklyProgressContainer>
        <TextSkeleton width="100px" height="22px" />
        <SkeletonBar height="10px" />
        <TextSkeleton width="60px" height="18px" />
      </WeeklyProgressContainer>

      <WeeklyProgressContainer>
        <TextSkeleton width="100px" height="22px" />
        <SkeletonBar height="10px" />
        <TextSkeleton width="60px" height="18px" />
      </WeeklyProgressContainer>
    </ReadingStatusCardContainer>
  );
};

export default ReadingStatusCardSkeleton;

const SkeletonBar = styled.div<{ height: string }>`
  width: 100%;
  height: ${({ height }) => height};
  border-radius: 8px;

  ${skeletonStyle}
`;

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
