import styled from '@emotion/styled';
import { Container, InfoBox } from './ImageInfoCard';
import SkeletonBox from '../Skeleton/SkeletonBox';
import { skeletonStyle } from '@/styles/skeleton';

const ImageInfoCardSkeleton = () => {
  return (
    <Container>
      <ImageSkeleton />
      <InfoBox>
        <SkeletonBox width="60%" height="20px" />
        <SkeletonBox width="80%" height="16px" />
      </InfoBox>
    </Container>
  );
};

export default ImageInfoCardSkeleton;

const ImageSkeleton = styled.div`
  width: 64px;
  height: 64px;
  border-radius: 16px;

  flex-shrink: 0;

  ${skeletonStyle}
`;
