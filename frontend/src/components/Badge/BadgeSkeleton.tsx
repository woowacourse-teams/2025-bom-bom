import styled from '@emotion/styled';
import { skeletonStyle } from '@/styles/skeleton';

interface BadgeSkeletonProps {
  skeletonHeight?: string;
}

const BadgeSkeleton = ({ skeletonHeight = '100%' }: BadgeSkeletonProps) => {
  return <Container height={skeletonHeight} />;
};

export default BadgeSkeleton;

const Container = styled.div<{ height: string }>`
  width: 36px;
  height: ${({ height }) => height};
  border-radius: 8px;

  ${skeletonStyle}
`;
