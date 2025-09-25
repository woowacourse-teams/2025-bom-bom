import styled from '@emotion/styled';
import TextSkeleton from '../Skeleton/TextSkeleton';
import type { ReactNode } from 'react';

interface TabSkeletonProps {
  StartComponent?: ReactNode;
  EndComponent?: ReactNode;
  textAlign?: 'start' | 'center' | 'end';
  skeletonWidth?: string;
  skeletonHeight?: string;
}

const TabSkeleton = ({
  StartComponent,
  EndComponent,
  textAlign = 'center',
  skeletonWidth = '100%',
  skeletonHeight = '100%',
}: TabSkeletonProps) => {
  return (
    <SkeletonContainer>
      {StartComponent}
      <SkeletonLabel textAlign={textAlign}>
        <TextSkeleton width={skeletonWidth} height={skeletonHeight} />
      </SkeletonLabel>
      {EndComponent}
    </SkeletonContainer>
  );
};

export default TabSkeleton;

const SkeletonContainer = styled.li`
  padding: 8px 12px;
  border-radius: 12px;

  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
`;

const SkeletonLabel = styled.span<{ textAlign: 'start' | 'center' | 'end' }>`
  width: 100%;
  text-align: ${({ textAlign }) => textAlign};
`;
