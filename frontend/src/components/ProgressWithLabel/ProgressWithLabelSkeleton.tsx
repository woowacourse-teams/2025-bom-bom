import styled from '@emotion/styled';
import ProgressBarSkeleton from '../ProgressBar/ProgressBarSkeleton';
import SkeletonBox from '../Skeleton/SkeletonBox';
import { useDevice } from '@/hooks/useDevice';

interface ProgressWithLabelSkeletonProps {
  hasShowGraph?: boolean;
  hasShowDescription?: boolean;
}

const ProgressWithLabelSkeleton = ({
  hasShowGraph = true,
  hasShowDescription = true,
}: ProgressWithLabelSkeletonProps) => {
  const device = useDevice();

  return (
    <SkeletonContainer>
      <SkeletonProgressInfo>
        <SkeletonBox width="80px" height="16px" />
        <SkeletonBox width="50px" height="16px" />
      </SkeletonProgressInfo>
      {hasShowGraph && <ProgressBarSkeleton />}
      {hasShowDescription && device === 'pc' && (
        <SkeletonBox width="100px" height="14px" />
      )}
    </SkeletonContainer>
  );
};

export default ProgressWithLabelSkeleton;

const SkeletonContainer = styled.div`
  width: 100%;

  display: flex;
  gap: 14px;
  flex-direction: column;
`;

const SkeletonProgressInfo = styled.div`
  width: 100%;

  display: flex;
  gap: 8px;
  align-items: center;

  > :last-child {
    margin-left: auto;
  }
`;
