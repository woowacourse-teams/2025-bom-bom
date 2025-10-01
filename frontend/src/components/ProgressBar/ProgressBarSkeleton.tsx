import { Container, ProgressCaption, ProgressGauge } from './ProgressBar';
import SkeletonBox from '../Skeleton/Skeleton';

interface ProgressBarSkeletonProps {
  hasCaption?: boolean;
}

const ProgressBarSkeleton = ({ hasCaption }: ProgressBarSkeletonProps) => {
  return (
    <Container>
      <SkeletonBox
        width="100%"
        height="100%"
        borderRadius="10px"
        as={ProgressGauge}
      />
      {hasCaption && (
        <SkeletonBox
          width="24px"
          height="12px"
          alignSelf="flex-end"
          as={ProgressCaption}
        />
      )}
    </Container>
  );
};

export default ProgressBarSkeleton;
