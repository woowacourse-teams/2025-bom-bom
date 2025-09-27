import styled from '@emotion/styled';
import { skeletonStyle } from '@/styles/skeleton';

interface TextSkeletonProps {
  width: string;
  height: string;
}

const SkeletonBox = ({ width, height }: TextSkeletonProps) => {
  return <Skeleton width={width} height={height} />;
};

export default SkeletonBox;

const Skeleton = styled.div<{ width: string; height: string }>`
  width: ${({ width }) => width};
  height: ${({ height }) => height};
  border-radius: 4px;

  ${skeletonStyle}
`;
