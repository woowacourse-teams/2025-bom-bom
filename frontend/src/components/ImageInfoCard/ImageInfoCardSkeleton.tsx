import styled from '@emotion/styled';
import SkeletonBox from '../Skeleton/SkeletonBox';
import { skeletonStyle } from '@/styles/skeleton';

const ImageInfoCardSkeleton = ({ ...props }) => {
  return (
    <Container {...props}>
      <ImageSkeleton />
      <InfoBox>
        <SkeletonBox width="60%" height="20px" />
        <SkeletonBox width="80%" height="16px" />
      </InfoBox>
    </Container>
  );
};

export default ImageInfoCardSkeleton;

const Container = styled.div`
  width: 100%;
  padding: 0;

  display: flex;
  gap: 12px;
  align-items: center;

  text-align: left;
`;

const ImageSkeleton = styled.div`
  width: 64px;
  height: 64px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgb(0 0 0 / 10%);

  flex-shrink: 0;

  ${skeletonStyle}
`;

const InfoBox = styled.div`
  min-height: 64px;

  display: flex;
  gap: 8px;
  flex: 1;
  flex-direction: column;
  justify-content: center;
`;
