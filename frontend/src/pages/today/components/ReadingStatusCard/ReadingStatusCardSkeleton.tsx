import styled from '@emotion/styled';
import ReadingStatusCardContainer from './ReadingStatusCardContainer';
import ProgressWithLabelSkeleton from '@/components/ProgressWithLabel/ProgressWithLabelSkeleton';
import { useDevice } from '@/hooks/useDevice';
import { skeletonStyle } from '@/styles/skeleton';

const ReadingStatusCardSkeleton = () => {
  const device = useDevice();
  const isPC = device === 'pc';

  return (
    <ReadingStatusCardContainer>
      <Streak />

      <SkeletonProgressWrapper isPC={isPC}>
        <ProgressWithLabelSkeleton
          hasShowGraph={isPC}
          hasShowDescription={isPC}
        />

        <ProgressWithLabelSkeleton
          hasShowGraph={isPC}
          hasShowDescription={isPC}
        />
      </SkeletonProgressWrapper>
    </ReadingStatusCardContainer>
  );
};

export default ReadingStatusCardSkeleton;

const Streak = styled.div`
  width: 104px;
  height: 104px;
  padding: 20px;
  border-radius: 12px;

  display: flex;
  align-items: center;
  justify-content: center;

  ${skeletonStyle}
`;

const SkeletonProgressWrapper = styled.div<{ isPC: boolean }>`
  width: 100%;

  display: flex;
  gap: ${({ isPC }) => (isPC ? '50px' : '16px')};
  flex-direction: column;
`;
