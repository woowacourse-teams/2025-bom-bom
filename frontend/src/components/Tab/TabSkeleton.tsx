import styled from '@emotion/styled';
import { ReactNode } from 'react';
import { Container, Label } from './Tab';
import { skeletonStyle } from '@/styles/skeleton';

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
    <Container selected={false}>
      {StartComponent}
      <Label textAlign={textAlign}>
        <SkeletonText height={skeletonHeight} />
      </Label>
      {EndComponent}
    </Container>
  );
};

export default TabSkeleton;

const SkeletonText = styled.div<{ height: string }>`
  width: 100%;
  height: ${({ height }) => height};
  border-radius: 4px;

  ${skeletonStyle}
`;
