import styled from '@emotion/styled';
import { Container, ProgressCaption, ProgressGauge } from './ProgressBar';
import { skeletonStyle } from '@/styles/skeleton';

const ProgressBarSkeleton = () => {
  return (
    <Container>
      <SkeletonProgressGauge as={ProgressGauge} />
      <SkeletonProgressCaption as={ProgressCaption} />
    </Container>
  );
};

export default ProgressBarSkeleton;

const SkeletonProgressGauge = styled.div`
  width: 100%;
  border-radius: 10px;

  ${skeletonStyle}
`;

const SkeletonProgressCaption = styled.div`
  width: 24px;
  height: 12px;
  margin-top: 4px;
  margin-left: auto;
  border-radius: 4px;

  ${skeletonStyle}
`;
