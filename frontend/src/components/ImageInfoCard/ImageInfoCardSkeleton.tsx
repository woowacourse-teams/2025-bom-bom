import { Container, InfoBox } from './ImageInfoCard';
import SkeletonBox from '../Skeleton/SkeletonBox';

const ImageInfoCardSkeleton = () => {
  return (
    <Container>
      <SkeletonBox width="64px" height="64px" borderRadius="16px" />
      <InfoBox>
        <SkeletonBox width="60%" height="20px" />
        <SkeletonBox width="80%" height="16px" />
      </InfoBox>
    </Container>
  );
};

export default ImageInfoCardSkeleton;
