import styled from '@emotion/styled';
import { skeletonStyle } from '@/styles/skeleton';

interface TextSkeletonProps {
  width: string;
  height: string;
  borderRadius?: string;
}

const SkeletonBox = ({
  width,
  height,
  borderRadius = '4px',
}: TextSkeletonProps) => {
  return <Skeleton width={width} height={height} borderRadius={borderRadius} />;
};

export default SkeletonBox;

const Skeleton = styled.div<{
  width: string;
  height: string;
  borderRadius: string;
}>`
  width: ${({ width }) => width};
  height: ${({ height }) => height};
  border-radius: ${({ borderRadius }) => borderRadius};

  flex-shrink: 0;

  ${skeletonStyle}
`;
