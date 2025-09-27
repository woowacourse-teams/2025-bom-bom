import styled from '@emotion/styled';
import { skeletonStyle } from '@/styles/skeleton';

interface ProgressBarSkeletonProps {
  width?: string;
}

const ProgressBarSkeleton = ({ width = '100%' }: ProgressBarSkeletonProps) => {
  return <SkeletonContainer width={width} />;
};

export default ProgressBarSkeleton;

const SkeletonContainer = styled.div<{ width: string }>`
  width: ${({ width }) => width};
  height: 10px;
  border-radius: 10px;

  ${skeletonStyle}
`;
