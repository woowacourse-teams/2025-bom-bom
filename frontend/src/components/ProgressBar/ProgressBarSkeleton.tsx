import styled from '@emotion/styled';
import { Container, ProgressCaption, ProgressGauge } from './ProgressBar';
import SkeletonBox from '../Skeleton/SkeletonBox';

interface ProgressBarSkeletonProps {
  hasCaption?: boolean;
}

const ProgressBarSkeleton = ({ hasCaption }: ProgressBarSkeletonProps) => {
  return (
    <SkeletonContainer as={Container}>
      <SkeletonBox
        width="100%"
        height="100%"
        borderRadius="10px"
        as={ProgressGauge}
      />
      {hasCaption && (
        <SkeletonBox width="24px" height="12px" as={ProgressCaption} />
      )}
    </SkeletonContainer>
  );
};

export default ProgressBarSkeleton;

const SkeletonContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-end;
`;
