import styled from '@emotion/styled';
import ReadingStatusCardContainer from './ReadingStatusCardContainer';
import TextSkeleton from '@/components/Skeleton/TextSkeleton';
import { skeletonStyle } from '@/styles/skeleton';

function ReadingStatusCardSkeleton() {
  return (
    <ReadingStatusCardContainer>
      <StreakWrapper>
        <StreakIcon />
        <TextSkeleton width="60px" height="28px" />
      </StreakWrapper>

      <ProgressSection>
        <TextSkeleton width="100px" height="22px" />
        <SkeletonBar height="10px" />
        <TextSkeleton width="60px" height="18px" />
      </ProgressSection>

      <ProgressSection>
        <TextSkeleton width="100px" height="22px" />
        <SkeletonBar height="10px" />
        <TextSkeleton width="60px" height="18px" />
      </ProgressSection>
    </ReadingStatusCardContainer>
  );
}

export default ReadingStatusCardSkeleton;

const SkeletonBar = styled.div<{ height?: string }>`
  width: 100%;
  height: ${({ height }) => height};
  border-radius: 8px;

  ${skeletonStyle}
`;

const StreakWrapper = styled.div`
  display: flex;
  gap: 10px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
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

const ProgressSection = styled.div`
  width: 100%;

  display: flex;
  gap: 14px;
  flex-direction: column;
`;
