import { Container, InfoBox } from './ImageInfoCard';
import Skeleton from '../Skeleton/Skeleton';

const ImageInfoCardSkeleton = () => {
  return (
    <Container>
      <Skeleton width="64px" height="64px" borderRadius="16px" />
      <InfoBox>
        <Skeleton width="60%" height="20px" />
        <Skeleton width="80%" height="16px" />
      </InfoBox>
    </Container>
  );
};

export default ImageInfoCardSkeleton;
