import styled from '@emotion/styled';
import { skeletonStyle } from '@/styles/skeleton';
import type { CSSProperties } from 'react';

interface SkeletonProps extends CSSProperties {
  width: string;
  height: string;
  as?: React.ElementType;
}

const Skeleton = ({ width, height, as, ...cssProps }: SkeletonProps) => {
  return <Container width={width} height={height} as={as} style={cssProps} />;
};

export default Skeleton;

const Container = styled.div<{
  width: string;
  height: string;
}>`
  width: ${({ width }) => width};
  height: ${({ height }) => height};
  border-radius: 4px;

  flex-shrink: 0;

  ${skeletonStyle}
`;
