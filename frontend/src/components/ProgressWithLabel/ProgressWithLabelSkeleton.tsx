import { Container, ProgressInfo } from './ProgressWithLabel';
import ProgressBarSkeleton from '../ProgressBar/ProgressBarSkeleton';
import Skeleton from '../Skeleton/Skeleton';

interface ProgressWithLabelSkeletonProps {
  hasShowGraph?: boolean;
  hasShowDescription?: boolean;
}

const ProgressWithLabelSkeleton = ({
  hasShowGraph = true,
  hasShowDescription = true,
}: ProgressWithLabelSkeletonProps) => {
  return (
    <Container>
      <ProgressInfo>
        <Skeleton width="80px" height="16px" />
      </ProgressInfo>
      {hasShowGraph && <ProgressBarSkeleton />}
      {hasShowDescription && <Skeleton width="100px" height="14px" />}
    </Container>
  );
};

export default ProgressWithLabelSkeleton;
