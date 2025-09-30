import { Container, ProgressInfo } from './ProgressWithLabel';
import ProgressBarSkeleton from '../ProgressBar/ProgressBarSkeleton';
import SkeletonBox from '../Skeleton/SkeletonBox';

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
        <SkeletonBox width="80px" height="16px" />
      </ProgressInfo>
      {hasShowGraph && <ProgressBarSkeleton />}
      {hasShowDescription && <SkeletonBox width="100px" height="14px" />}
    </Container>
  );
};

export default ProgressWithLabelSkeleton;
