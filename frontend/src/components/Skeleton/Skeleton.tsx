import styled from '@emotion/styled';
import { shimmer } from './Skeleton.keyframes';
import type { CSSProperties } from 'react';

interface SkeletonProps extends CSSProperties {
  as?: React.ElementType;
}

const Skeleton = ({ as, ...cssProps }: SkeletonProps) => {
  return <Container as={as} style={cssProps} />;
};

export default Skeleton;

const Container = styled.div`
  border-radius: 4px;

  flex-shrink: 0;

  background: linear-gradient(90deg, #e0e0e0 25%, #f0f0f0 37%, #e0e0e0 63%);
  background-size: 400px 100%;

  animation: ${shimmer} 1.4s ease infinite;
`;
