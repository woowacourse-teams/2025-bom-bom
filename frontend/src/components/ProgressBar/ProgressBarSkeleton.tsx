import { Container, ProgressCaption, ProgressGauge } from './ProgressBar';
import Skeleton from '../Skeleton/Skeleton';

interface ProgressBarSkeletonProps {
  hasCaption?: boolean;
}

const ProgressBarSkeleton = ({ hasCaption }: ProgressBarSkeletonProps) => {
  return (
    <Container>
      <Skeleton
        width="100%"
        height="100%"
        borderRadius="10px"
        as={ProgressGauge}
      />
      {hasCaption && (
        <Skeleton
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
