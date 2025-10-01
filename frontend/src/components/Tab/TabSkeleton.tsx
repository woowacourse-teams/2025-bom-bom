import styled from '@emotion/styled';
import { Container, Label } from './Tab';
import Skeleton from '../Skeleton/Skeleton';

interface TabSkeletonProps {
  StartComponentSkeleton?: {
    width: string;
    height: string;
    borderRadius?: string;
  };
  EndComponentSkeleton?: {
    width: string;
    height: string;
    borderRadius?: string;
  };
  textAlign?: 'start' | 'center' | 'end';
  width?: string;
  height?: string;
}

const TabSkeleton = ({
  StartComponentSkeleton,
  EndComponentSkeleton,
  textAlign = 'center',
  width = '100%',
  height = '100%',
}: TabSkeletonProps) => {
  return (
    <SkeletonContainer as={Container}>
      {StartComponentSkeleton && (
        <Skeleton
          width={StartComponentSkeleton.width}
          height={StartComponentSkeleton.height}
          borderRadius={StartComponentSkeleton.borderRadius}
        />
      )}
      <Label textAlign={textAlign}>
        <Skeleton width={width} height={height} />
      </Label>
      {EndComponentSkeleton && (
        <Skeleton
          width={EndComponentSkeleton.width}
          height={EndComponentSkeleton.height}
          borderRadius={EndComponentSkeleton.borderRadius}
        />
      )}
    </SkeletonContainer>
  );
};

export default TabSkeleton;

const SkeletonContainer = styled.li`
  padding: 8px 12px;
`;
