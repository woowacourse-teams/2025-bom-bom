import styled from '@emotion/styled';
import { skeletonStyle } from '@/styles/skeleton';
import type { CSSProperties } from 'react';

interface TextSkeletonProps extends CSSProperties {
  width: string;
  height: string;
  as?: React.ElementType;
}

const SkeletonBox = ({ width, height, as, ...cssProps }: TextSkeletonProps) => {
  return <Skeleton width={width} height={height} as={as} style={cssProps} />;
};

export default SkeletonBox;

const Skeleton = styled.div<{
  width: string;
  height: string;
}>`
  width: ${({ width }) => width};
  height: ${({ height }) => height};
  border-radius: 4px;

  flex-shrink: 0;

  ${skeletonStyle}
`;
