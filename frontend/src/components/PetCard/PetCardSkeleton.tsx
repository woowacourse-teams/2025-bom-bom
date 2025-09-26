import styled from '@emotion/styled';
import PetCardContainer from './PetCardContainer';
import TextSkeleton from '../Skeleton/TextSkeleton';
import { useDevice } from '@/hooks/useDevice';
import { skeletonStyle } from '@/styles/skeleton';
import type { Device } from '@/hooks/useDevice';

const PetCardSkeleton = () => {
  const device = useDevice();

  return (
    <PetCardContainer>
      <PetImageContainer>
        <SkeletonPetImage />
      </PetImageContainer>

      <TextSkeleton width="100px" height="16px" />

      <ProgressBarContainer>
        <SkeletonProgressBar />
        <TextSkeleton width="40px" height="12px" />
      </ProgressBarContainer>

      <SkeletonAttendanceButton device={device} />
    </PetCardContainer>
  );
};

export default PetCardSkeleton;

const PetImageContainer = styled.div`
  position: relative;

  display: flex;
  align-items: center;
  justify-content: center;
`;

const SkeletonPetImage = styled.div`
  width: 120px;
  height: 120px;
  border-radius: 12px;

  ${skeletonStyle}
`;

const ProgressBarContainer = styled.div`
  width: 100%;

  display: flex;
  gap: 4px;
  flex-direction: column;
  align-items: flex-end;
`;

const SkeletonProgressBar = styled.div`
  width: 100%;
  height: 12px;
  border-radius: 4px;

  ${skeletonStyle}
`;

const SkeletonAttendanceButton = styled.div<{ device: Device }>`
  width: 50%;
  height: 28px;
  margin-top: -8px;
  padding: 8px 16px;
  border-radius: 8px;

  ${({ device }) =>
    device === 'mobile' && {
      position: 'absolute',
      left: '50%',
      bottom: 0,
      width: '50%',
      transform: 'translateX(-50%)',
    }}

  ${skeletonStyle};
`;
