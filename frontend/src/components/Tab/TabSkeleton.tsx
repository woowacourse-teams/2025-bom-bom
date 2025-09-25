import styled from '@emotion/styled';
import { skeletonStyle } from '@/styles/skeleton';
import type { ReactNode } from 'react';

interface TabSkeletonProps {
  StartComponent?: ReactNode;
  EndComponent?: ReactNode;
  textAlign?: 'start' | 'center' | 'end';
  skeletonHeight?: string;
}

const TabSkeleton = ({
  StartComponent,
  EndComponent,
  textAlign = 'center',
  skeletonHeight = '100%',
}: TabSkeletonProps) => {
  return (
    <SkeletonContainer selected={false}>
      {StartComponent}
      <SkeletonLabel textAlign={textAlign}>
        <SkeletonText height={skeletonHeight} />
      </SkeletonLabel>
      {EndComponent}
    </SkeletonContainer>
  );
};

export default TabSkeleton;

const SkeletonContainer = styled.li<{ selected: boolean }>`
  padding: 10px 12px;
  border-radius: 12px;

  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
`;

const SkeletonText = styled.div<{ height: string }>`
  width: 100%;
  height: ${({ height }) => height};
  border-radius: 4px;

  ${skeletonStyle}
`;

const SkeletonLabel = styled.span<{ textAlign: 'start' | 'center' | 'end' }>`
  width: 100%;
  text-align: ${({ textAlign }) => textAlign};
`;
